package com.servify.usuarios.application.dto;

import com.servify.shared.domain.enumtype.ModalidadServicio;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PrestadorPublicoResult {

    private final UUID usuarioId;
    private final String nombreUsuario;
    private final String nombre;
    private final String apellido;
    private final String fotoPerfilUrl;
    private final String descripcionPersonal;
    private final String localidad;
    private final Integer cantidadPublicacionesActivas;
    private final List<String> categorias;
    private final List<String> servicios;
    private final List<String> zonasCobertura;
    private final BigDecimal precioDesde;
    private final List<PublicacionActivaResult> publicacionesActivas;
    private final Integer cantidadValoraciones;
    private final Double promedioEstrellas;

    public PrestadorPublicoResult(
            UUID usuarioId,
            String nombreUsuario,
            String nombre,
            String apellido,
            String fotoPerfilUrl,
            String descripcionPersonal,
            String localidad,
            Integer cantidadPublicacionesActivas,
            List<String> categorias,
            List<String> servicios,
            List<String> zonasCobertura,
            BigDecimal precioDesde,
            List<PublicacionActivaResult> publicacionesActivas,
            Integer cantidadValoraciones,
            Double promedioEstrellas
    ) {
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.descripcionPersonal = descripcionPersonal;
        this.localidad = localidad;
        this.cantidadPublicacionesActivas = cantidadPublicacionesActivas;
        this.categorias = categorias;
        this.servicios = servicios;
        this.zonasCobertura = zonasCobertura;
        this.precioDesde = precioDesde;
        this.publicacionesActivas = publicacionesActivas;
        this.cantidadValoraciones = cantidadValoraciones;
        this.promedioEstrellas = promedioEstrellas;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public String getDescripcionPersonal() {
        return descripcionPersonal;
    }

    public String getLocalidad() {
        return localidad;
    }

    public Integer getCantidadPublicacionesActivas() {
        return cantidadPublicacionesActivas;
    }

    public List<String> getCategorias() {
        return categorias;
    }

    public List<String> getServicios() {
        return servicios;
    }

    public List<String> getZonasCobertura() {
        return zonasCobertura;
    }

    public BigDecimal getPrecioDesde() {
        return precioDesde;
    }

    public List<PublicacionActivaResult> getPublicacionesActivas() {
        return publicacionesActivas;
    }

    public Integer getCantidadValoraciones() {
        return cantidadValoraciones;
    }

    public Double getPromedioEstrellas() {
        return promedioEstrellas;
    }

    public static class PublicacionActivaResult {
        private final UUID id;
        private final String titulo;
        private final String descripcion;
        private final String categoria;
        private final ModalidadServicio modalidadServicio;
        private final List<String> zonasCobertura;
        private final BigDecimal precioBase;

        public PublicacionActivaResult(
                UUID id,
                String titulo,
                String descripcion,
                String categoria,
                ModalidadServicio modalidadServicio,
                List<String> zonasCobertura,
                BigDecimal precioBase
        ) {
            this.id = id;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.categoria = categoria;
            this.modalidadServicio = modalidadServicio;
            this.zonasCobertura = zonasCobertura;
            this.precioBase = precioBase;
        }

        public UUID getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public String getCategoria() {
            return categoria;
        }

        public ModalidadServicio getModalidadServicio() {
            return modalidadServicio;
        }

        public List<String> getZonasCobertura() {
            return zonasCobertura;
        }

        public BigDecimal getPrecioBase() {
            return precioBase;
        }
    }
}
