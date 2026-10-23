import React, { useState, useEffect, useRef, useCallback } from 'react';
import './App.css';
import AcortarForm from './components/AcortarForm';
import TablaEnlaces from './components/TablaEnlaces';
import CountdownCache from './components/CountdownCache';

const BACKEND_HOST = (typeof window !== 'undefined' && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'))
  ? 'localhost:8081'
  : (typeof window !== 'undefined' ? window.location.host : 'localhost:8081');

const API_BASE = `http://${BACKEND_HOST}/api/enlaces`;
const WS_URL = `ws://${BACKEND_HOST}/ws/cache`;

/**
 * Componente Principal: App.
 *
 * Rol en el Frontend:
 * Coordina la vista principal, el estado de navegación ('acortar' | 'ver'),
 * la conexión persistente con WebSocket para el TTL real de Redis,
 * y la carga de datos respetando las reglas de negocio:
 * - Primera consulta (cache miss): no muestra cuenta regresiva.
 * - Segunda consulta (cache hit): muestra cuenta regresiva activa.
 * - Recarga del navegador: recupera el tiempo real sin reiniciar.
 * - Expiración en 0: solicita nuevamente el listado a la API.
 */
export default function App() {
  const [seccion, setSeccion] = useState('acortar'); // 'acortar' | 'ver'
  const [enlaces, setEnlaces] = useState([]);
  const [cargando, setCargando] = useState(false);
  const [wsConectado, setWsConectado] = useState(false);
  
  // Estado recibido exclusivamente desde el WebSocket (TTL real de Redis)
  const [cacheInfo, setCacheInfo] = useState({ cacheado: false, segundosRestantes: 0 });

  // Control para ocultar cuenta regresiva en la primera consulta (cache miss)
  // y mostrarla a partir de la segunda consulta (o al recargar si ya está cacheado)
  const [mostrarCuentaRegresiva, setMostrarCuentaRegresiva] = useState(false);
  const contadorConsultasRef = useRef(0);
  const wsRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const expiracionDisparadaRef = useRef(false);

  /**
   * Función para consultar el listado de enlaces desde el backend: GET /api/enlaces
   */
  const cargarEnlaces = useCallback(async () => {
    try {
      setCargando(true);
      const res = await fetch(API_BASE);
      if (!res.ok) {
        throw new Error('Error al obtener la lista de enlaces');
      }
      const data = await res.json();
      setEnlaces(data);

      contadorConsultasRef.current += 1;

      // Regla: En la PRIMERA consulta (cache miss) no se muestra cuenta regresiva.
      // Aparece desde la SEGUNDA consulta, cuando ya está cacheado.
      if (contadorConsultasRef.current >= 2) {
        setMostrarCuentaRegresiva(true);
      }
    } catch (err) {
      console.error('Error al cargar enlaces:', err);
    } finally {
      setCargando(false);
    }
  }, []);

  /**
   * Conexión WebSocket resiliente con reconexión automática
   */
  useEffect(() => {
    let activo = true;

    const conectarWebSocket = () => {
      try {
        const socket = new WebSocket(WS_URL);
        wsRef.current = socket;

        socket.onopen = () => {
          if (!activo) return;
          console.log('[WebSocket] Conectado a', WS_URL);
          setWsConectado(true);
        };

        socket.onmessage = (event) => {
          if (!activo) return;
          try {
            const data = JSON.parse(event.data);
            // Formato esperado: {"cacheado": true, "segundosRestantes": 27} o {"cacheado": false}
            setCacheInfo(data);

            if (data.cacheado && data.segundosRestantes > 0) {
              expiracionDisparadaRef.current = false;

              // Regla de Recarga: Si el usuario recarga el navegador y la clave ya está en Redis,
              // recuperamos el tiempo restante inmediatamente desde el servidor
              if (contadorConsultasRef.current === 0) {
                // Al recargar estando ya cacheado, se permite mostrar
                setMostrarCuentaRegresiva(true);
              }
            } else {
              // TTL <= 0 o cacheado === false
              setMostrarCuentaRegresiva(false);

              // Regla: Cuando expira (llega a 0), el frontend vuelve a pedir el listado,
              // lo que genera un nuevo cache miss y consulta Mongo.
              if (!expiracionDisparadaRef.current && contadorConsultasRef.current > 0) {
                expiracionDisparadaRef.current = true;
                contadorConsultasRef.current = 1; // Reinicia a 1ra consulta (miss)
                console.log('[Caché Expiró] Solicitando nuevo listado para generar Cache Miss en Mongo');
                cargarEnlaces();
              }
            }
          } catch (err) {
            console.error('[WebSocket] Error al procesar mensaje:', err);
          }
        };

        socket.onclose = () => {
          if (!activo) return;
          console.warn('[WebSocket] Conexión cerrada. Reconectando en 2 segundos...');
          setWsConectado(false);
          reconnectTimeoutRef.current = setTimeout(conectarWebSocket, 2000);
        };

        socket.onerror = (err) => {
          console.error('[WebSocket] Error de conexión:', err);
          socket.close();
        };
      } catch (e) {
        if (!activo) return;
        reconnectTimeoutRef.current = setTimeout(conectarWebSocket, 2000);
      }
    };

    conectarWebSocket();

    return () => {
      activo = false;
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, [cargarEnlaces]);

  // Al cambiar a la pestaña 'ver', cargar los enlaces
  const cambiarASeccionVer = () => {
    setSeccion('ver');
    cargarEnlaces();
  };

  const cambiarASeccionAcortar = () => {
    setSeccion('acortar');
  };

  const handleEnlaceCreado = () => {
    // Al acortar un enlace, la clave de Redis queda invalidada por el backend.
    // Reiniciamos contador para que la próxima lectura sea una primera consulta (cache miss).
    contadorConsultasRef.current = 0;
    setMostrarCuentaRegresiva(false);
  };

  return (
    <div className="app-container">
      {/* Header */}
      <header className="app-header">
        <div className="brand-wrapper">
          <svg className="brand-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
          </svg>
          <h1 className="app-title">Acortador de Enlaces</h1>
        </div>
        <p className="app-subtitle">
          Arquitectura Hexagonal &bull; Abstract Factory &bull; MongoDB &bull; Redis &bull; WebSocket
        </p>
        <div>
          <span className={`ws-status-badge ${wsConectado ? 'connected' : ''}`}>
            <span className="status-dot"></span>
            {wsConectado ? 'WebSocket en vivo (/ws/cache)' : 'Conectando WebSocket...'}
          </span>
        </div>
      </header>

      {/* Selector de Sección: Dos botones juntos, el activo se resalta */}
      <nav className="nav-buttons-container" aria-label="Navegación principal">
        <div className="nav-buttons-group">
          <button
            type="button"
            className={`btn-nav ${seccion === 'acortar' ? 'active' : ''}`}
            onClick={cambiarASeccionAcortar}
          >
            ✏️ Acortar enlace
          </button>
          <button
            type="button"
            className={`btn-nav ${seccion === 'ver' ? 'active' : ''}`}
            onClick={cambiarASeccionVer}
          >
            📋 Ver enlaces
          </button>
        </div>
      </nav>

      {/* Contenido Principal */}
      <main className="main-card">
        {seccion === 'acortar' && (
          <AcortarForm onEnlaceCreado={handleEnlaceCreado} />
        )}

        {seccion === 'ver' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {/* Cuenta Regresiva de Redis mediante WebSocket (visible si está cacheado y superó la 1ra consulta) */}
            {mostrarCuentaRegresiva && (
              <CountdownCache cacheInfo={cacheInfo} />
            )}

            <TablaEnlaces
              enlaces={enlaces}
              cargando={cargando}
              onRefrescar={cargarEnlaces}
            />
          </div>
        )}
      </main>
    </div>
  );
}
