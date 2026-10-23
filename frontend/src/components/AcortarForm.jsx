import React, { useState } from 'react';

/**
 * Componente: AcortarForm.
 *
 * Rol en el Frontend:
 * Sección "Acortar enlace". Permite al usuario ingresar una URL larga,
 * validarla en el cliente y enviarla al endpoint POST /api/enlaces del backend.
 *
 * Al recibir la respuesta:
 * - Despliega el enlace corto clickeable con target="_blank".
 * - Provee un botón para copiar directamente la URL generada al portapapeles.
 */
export default function AcortarForm({ onEnlaceCreado }) {
  const [url, setUrl] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState(null);
  const [resultado, setResultado] = useState(null);
  const [copiado, setCopiado] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setCopiado(false);

    const urlLimpia = url.trim();

    // Validación básica en cliente
    if (!urlLimpia) {
      setError('Por favor, ingresa una URL.');
      return;
    }

    if (!/^https?:\/\/.+/i.test(urlLimpia)) {
      setError('La URL debe comenzar con http:// o https:// y ser válida.');
      return;
    }

    try {
      setCargando(true);
      const host = (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')
        ? 'localhost:8081'
        : window.location.host;
      const respuesta = await fetch(`http://${host}/api/enlaces`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ enlaceOriginal: urlLimpia }),
      });

      if (!respuesta.ok) {
        const errorData = await respuesta.json().catch(() => null);
        const mensaje = errorData?.mensaje || 'Error al acortar el enlace. Verifica los datos.';
        throw new Error(mensaje);
      }

      const enlaceCreado = await respuesta.json();
      setResultado(enlaceCreado);
      setUrl('');

      if (onEnlaceCreado) {
        onEnlaceCreado(enlaceCreado);
      }
    } catch (err) {
      setError(err.message || 'Error de conexión con el servidor backend.');
    } finally {
      setCargando(false);
    }
  };

  const copiarAlPortapapeles = async () => {
    if (!resultado?.enlaceAcortado) return;
    try {
      await navigator.clipboard.writeText(resultado.enlaceAcortado);
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2500);
    } catch {
      // Fallback si clipboard API no estuviera disponible
      const textarea = document.createElement('textarea');
      textarea.value = resultado.enlaceAcortado;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2500);
    }
  };

  return (
    <div className="acortar-section">
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="url-input" className="form-label">
            Pega tu enlace largo aquí:
          </label>
          <div className="input-with-button">
            <input
              id="url-input"
              type="text"
              className="url-input font-mono"
              placeholder="https://ejemplo.com/articulo-muy-largo-con-parametros"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              disabled={cargando}
              autoComplete="off"
            />
            <button
              type="submit"
              className="btn-primary"
              disabled={cargando || !url.trim()}
            >
              {cargando ? 'Acortando...' : 'Acortar'}
            </button>
          </div>
        </div>
      </form>

      {error && (
        <div className="alert-error" role="alert">
          {error}
        </div>
      )}

      {resultado && (
        <div className="result-card">
          <div className="result-header">¡Enlace acortado con éxito!</div>
          <div className="result-row">
            <a
              href={resultado.enlaceAcortado}
              target="_blank"
              rel="noopener noreferrer"
              className="result-url font-mono"
            >
              {resultado.enlaceAcortado}
            </a>
            <div className="result-actions">
              <button
                type="button"
                className={`btn-copy ${copiado ? 'copied' : ''}`}
                onClick={copiarAlPortapapeles}
              >
                {copiado ? '✓ Copiado' : 'Copiar enlace'}
              </button>
            </div>
          </div>
          <div className="result-original-info">
            <strong>Destino original:</strong> {resultado.enlaceOriginal}
          </div>
        </div>
      )}
    </div>
  );
}
