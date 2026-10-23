import React from 'react';

/**
 * Componente: CountdownCache.
 *
 * Rol en el Frontend:
 * Muestra la cuenta regresiva visual del tiempo de expiración del caché en Redis.
 *
 * Reglas de Visualización:
 * - Lee ÚNICAMENTE el TTL real emitido por el backend a través de Redis (comando TTL).
 * - NUNCA utiliza un setInterval o temporizador local independiente en React.
 * - Visible SOLO cuando 'cacheado === true' y 'segundosRestantes > 0'.
 * - Si no está en caché (cache miss inicial o expiración), no se renderiza.
 */
export default function CountdownCache({ cacheInfo }) {
  if (!cacheInfo || !cacheInfo.cacheado || cacheInfo.segundosRestantes <= 0) {
    return null;
  }

  return (
    <div className="cache-countdown-banner" role="status" aria-live="polite">
      <div className="cache-info">
        <span className="cache-flash-icon">⚡</span>
        <span>Caché activo en Redis (lecturas no tocan la BD)</span>
      </div>
      <div className="cache-timer">
        Caché expira en: <strong>{cacheInfo.segundosRestantes} s</strong>
      </div>
    </div>
  );
}
