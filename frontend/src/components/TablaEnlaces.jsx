import React from 'react';

/**
 * Componente: TablaEnlaces.
 *
 * Rol en el Frontend:
 * Sección "Ver enlaces".
 * Despliega la tabla con las columnas obligatorias:
 * - id
 * - enlace original
 * - enlace acortado (<a> funcional con target="_blank" hacia el backend)
 */
export default function TablaEnlaces({ enlaces, cargando, onRefrescar }) {
  return (
    <div className="ver-section">
      <div className="ver-header">
        <h2 className="ver-title">Enlaces Registrados</h2>
        <button
          type="button"
          className="btn-refresh"
          onClick={onRefrescar}
          disabled={cargando}
          title="Consultar nuevamente la API"
        >
          {cargando ? 'Consultando...' : '🔄 Actualizar listado'}
        </button>
      </div>

      {enlaces.length === 0 && !cargando ? (
        <div className="empty-state">
          <div className="empty-icon">🔗</div>
          <p className="empty-text">No hay enlaces registrados aún.</p>
        </div>
      ) : (
        <div className="table-wrapper">
          <table className="enlaces-table">
            <thead>
              <tr>
                <th style={{ width: '22%' }}>ID</th>
                <th style={{ width: '43%' }}>Enlace Original</th>
                <th style={{ width: '35%' }}>Enlace Acortado (Click para abrir)</th>
              </tr>
            </thead>
            <tbody>
              {enlaces.map((enlace) => (
                <tr key={enlace.id || enlace.enlaceAcortado}>
                  <td>
                    <span className="id-badge" title={enlace.id}>
                      {enlace.id || 'N/A'}
                    </span>
                  </td>
                  <td className="url-cell" title={enlace.enlaceOriginal}>
                    <a
                      href={enlace.enlaceOriginal}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="url-link-original"
                    >
                      {enlace.enlaceOriginal}
                    </a>
                  </td>
                  <td>
                    <a
                      href={enlace.enlaceAcortado}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="url-link-acortado font-mono"
                    >
                      {enlace.enlaceAcortado} ↗
                    </a>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
