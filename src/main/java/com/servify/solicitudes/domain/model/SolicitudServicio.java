package com.servify.solicitudes.domain.model;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.model.BaseEntity;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.enumtype.TipoProgramacionSolicitud;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

//Tomé estas decisiones para mantener coherencia:
//solicitanteId en vez de referencia a Usuario
//categoriaServicioId en vez de referencia a CategoriaServicio
//ModalidadServicio reutilizada como enum común del negocio
//DisponibilidadHoraria reutilizada como franja requerida
//precioReferencia como BigDecimal

public class SolicitudServicio extends BaseEntity {

    private UUID solicitanteId;
    private UUID categoriaServicioId;
    private ModalidadServicio modalidadServicio;
    private Ubicacion ubicacion;
    private DisponibilidadHoraria disponibilidadRequerida;
    private String descripcionNecesidad;
    private BigDecimal precioReferencia;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private TipoProgramacionSolicitud tipoProgramacion;
    private LocalDateTime fechaProgramadaInicio;
    private LocalDateTime fechaProgramadaFin;

    protected SolicitudServicio() {
    }

    public SolicitudServicio(UUID id,
                             UUID solicitanteId,
                             UUID categoriaServicioId,
                             ModalidadServicio modalidadServicio,
                             Ubicacion ubicacion,
                             DisponibilidadHoraria disponibilidadRequerida,
                             String descripcionNecesidad,
                             BigDecimal precioReferencia,
                             EstadoSolicitud estado,
                             LocalDateTime fechaSolicitud) {
        this(id, solicitanteId, categoriaServicioId, modalidadServicio, ubicacion, disponibilidadRequerida,
                descripcionNecesidad, precioReferencia, estado, fechaSolicitud,
                TipoProgramacionSolicitud.INMEDIATA, null, null);
    }

    public SolicitudServicio(UUID id,
                             UUID solicitanteId,
                             UUID categoriaServicioId,
                             ModalidadServicio modalidadServicio,
                             Ubicacion ubicacion,
                             DisponibilidadHoraria disponibilidadRequerida,
                             String descripcionNecesidad,
                             BigDecimal precioReferencia,
                             EstadoSolicitud estado,
                             LocalDateTime fechaSolicitud,
                             TipoProgramacionSolicitud tipoProgramacion,
                             LocalDateTime fechaProgramadaInicio,
                             LocalDateTime fechaProgramadaFin) {
        super(id);
        this.solicitanteId = solicitanteId;
        this.categoriaServicioId = categoriaServicioId;
        this.modalidadServicio = modalidadServicio;
        this.ubicacion = ubicacion;
        this.disponibilidadRequerida = disponibilidadRequerida;
        this.descripcionNecesidad = descripcionNecesidad;
        this.precioReferencia = precioReferencia;
        this.estado = estado;
        this.fechaSolicitud = fechaSolicitud;
        this.tipoProgramacion = tipoProgramacion != null ? tipoProgramacion : TipoProgramacionSolicitud.INMEDIATA;
        this.fechaProgramadaInicio = fechaProgramadaInicio;
        this.fechaProgramadaFin = fechaProgramadaFin;
        validarProgramacion();
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public UUID getCategoriaServicioId() {
        return categoriaServicioId;
    }

    public ModalidadServicio getModalidadServicio() {
        return modalidadServicio;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public DisponibilidadHoraria getDisponibilidadRequerida() {
        return disponibilidadRequerida;
    }

    public String getDescripcionNecesidad() {
        return descripcionNecesidad;
    }

    public BigDecimal getPrecioReferencia() {
        return precioReferencia;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public TipoProgramacionSolicitud getTipoProgramacion() {
        return tipoProgramacion;
    }

    public LocalDateTime getFechaProgramadaInicio() {
        return fechaProgramadaInicio;
    }

    public LocalDateTime getFechaProgramadaFin() {
        return fechaProgramadaFin;
    }

    public boolean esProgramada() {
        return tipoProgramacion == TipoProgramacionSolicitud.PROGRAMADA;
    }

    public boolean esRecurrente() {
        return tipoProgramacion == TipoProgramacionSolicitud.RECURRENTE;
    }

    public boolean estaBuscandoPrestador() {
        return this.estado == EstadoSolicitud.BUSCANDO_PRESTADOR;
    }

    public boolean estaAsignada() {
        return this.estado == EstadoSolicitud.ASIGNADA;
    }

    public boolean estaFinalizada() {
        return this.estado == EstadoSolicitud.FINALIZADA;
    }

    public boolean estaCancelada() {
        return this.estado == EstadoSolicitud.CANCELADA;
    }

    public boolean puedeSerCancelada() {
        return !estaFinalizada() && !estaCancelada();
    }

    public boolean puedeRecibirRespuestas() {
        return estaBuscandoPrestador();
    }

    public void actualizarModalidad(ModalidadServicio modalidadServicio) {
        if (modalidadServicio == null) {
            throw new IllegalArgumentException("modalidadServicio no puede ser nulo");
        }
        if (estaAsignada() || estaFinalizada()) {
            throw new IllegalStateException("No se puede cambiar la modalidad en el estado actual");
        }
        this.modalidadServicio = modalidadServicio;
    }

    public void actualizarUbicacion(Ubicacion ubicacion) {
        if (ubicacion == null) {
            throw new IllegalArgumentException("ubicacion no puede ser nula");
        }
        if (estaAsignada() || estaFinalizada()) {
            throw new IllegalStateException("No se puede cambiar la ubicación en el estado actual");
        }
        if (!ubicacion.esAptaParaBusquedaGeografica()) {
            throw new IllegalArgumentException("La ubicación no es apta para búsqueda geográfica");
        }
        this.ubicacion = ubicacion;
    }

    public void actualizarDisponibilidadRequerida(DisponibilidadHoraria disponibilidadRequerida) {
        if (disponibilidadRequerida == null) {
            throw new IllegalArgumentException("disponibilidadRequerida no puede ser nula");
        }
        if (estaAsignada() || estaFinalizada()) {
            throw new IllegalStateException("No se puede cambiar la disponibilidad en el estado actual");
        }
        if (!disponibilidadRequerida.esRangoHorarioValido()) {
            throw new IllegalArgumentException("La disponibilidad horaria no es válida");
        }
        this.disponibilidadRequerida = disponibilidadRequerida;
    }

    public void actualizarDescripcionNecesidad(String descripcionNecesidad) {
        if (descripcionNecesidad == null || descripcionNecesidad.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de la necesidad no puede ser nula o vacía");
        }
        if (descripcionNecesidad.length() > 2000) {
            throw new IllegalArgumentException("La descripción de la necesidad no puede exceder 2000 caracteres");
        }
        if (estaFinalizada()) {
            throw new IllegalStateException("No se puede modificar la descripción en una solicitud finalizada");
        }
        this.descripcionNecesidad = descripcionNecesidad;
    }

    public void actualizarPrecioReferencia(BigDecimal precioReferencia) {
        if (precioReferencia == null) {
            throw new IllegalArgumentException("precioReferencia no puede ser nulo");
        }
        if (precioReferencia.signum() < 0) {
            throw new IllegalArgumentException("El precio de referencia no puede ser negativo");
        }
        if (estaFinalizada()) {
            throw new IllegalStateException("No se puede modificar el precio de una solicitud finalizada");
        }
        this.precioReferencia = precioReferencia;
    }

    public void actualizarProgramacion(TipoProgramacionSolicitud tipoProgramacion,
                                       LocalDateTime fechaProgramadaInicio,
                                       LocalDateTime fechaProgramadaFin) {
        if (estaAsignada() || estaFinalizada()) {
            throw new IllegalStateException("No se puede cambiar la programacion en el estado actual");
        }
        this.tipoProgramacion = tipoProgramacion != null ? tipoProgramacion : TipoProgramacionSolicitud.INMEDIATA;
        this.fechaProgramadaInicio = fechaProgramadaInicio;
        this.fechaProgramadaFin = fechaProgramadaFin;
        validarProgramacion();
    }

    public void marcarComoBuscandoPrestador() {
        this.estado = EstadoSolicitud.BUSCANDO_PRESTADOR;
    }

    public void marcarComoAsignada() {
        this.estado = EstadoSolicitud.ASIGNADA;
    }

    public void marcarComoFinalizada() {
        this.estado = EstadoSolicitud.FINALIZADA;
    }

    public void cancelar() {
        if (!puedeSerCancelada()) {
            throw new IllegalStateException("La solicitud no puede ser cancelada en su estado actual");
        }
        this.estado = EstadoSolicitud.CANCELADA;
    }

    private void validarProgramacion() {
        if (tipoProgramacion == TipoProgramacionSolicitud.PROGRAMADA) {
            if (fechaProgramadaInicio == null || fechaProgramadaFin == null) {
                throw new IllegalArgumentException("Las solicitudes programadas requieren fecha de inicio y fin");
            }
            if (!fechaProgramadaInicio.isBefore(fechaProgramadaFin)) {
                throw new IllegalArgumentException("La fecha programada de inicio debe ser anterior a la fecha de fin");
            }
        }
        if (tipoProgramacion == TipoProgramacionSolicitud.INMEDIATA
                && fechaProgramadaInicio != null
                && fechaProgramadaFin != null
                && !fechaProgramadaInicio.isBefore(fechaProgramadaFin)) {
            throw new IllegalArgumentException("La fecha programada de inicio debe ser anterior a la fecha de fin");
        }
    }
}
