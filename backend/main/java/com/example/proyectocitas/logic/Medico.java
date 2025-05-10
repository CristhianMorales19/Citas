package com.example.proyectocitas.logic;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Entity
@Table(name = "medico", schema = "proyectocitas")
public class Medico {
    @Id
    @Column(name = "id_medico", nullable = false)
    private Integer id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    @Size(max = 75)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 75)
    private String nombre;

    @Size(max = 50)
    @NotNull
    @Column(name = "especialidad", nullable = false, length = 50)
    private String especialidad;

    @NotNull
    @Column(name = "costo_consulta", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoConsulta;

    @Size(max = 100)
    @NotNull
    @Column(name = "ubicacion", nullable = false, length = 100)
    private String ubicacion;

    @Lob
    @Column(name = "presentacion")
    private String presentacion;

    @NotNull
    @Column(name = "frecuencia_citas", nullable = false)
    private Integer frecuenciaCitas;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "autorizado", nullable = false)
    private Boolean autorizado = false;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "primera_vez", nullable = false)
    private Boolean primeraVez = false;

    @OneToMany(mappedBy = "id")
    private List<Horario> horarios = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Usuario idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public BigDecimal getCostoConsulta() {
        return costoConsulta;
    }

    public void setCostoConsulta(BigDecimal costoConsulta) {
        this.costoConsulta = costoConsulta;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public Integer getFrecuenciaCitas() {
        return frecuenciaCitas;
    }

    public void setFrecuenciaCitas(Integer frecuenciaCitas) {
        this.frecuenciaCitas = frecuenciaCitas;
    }

    public Boolean getAutorizado() {
        return autorizado;
    }

    public void setAutorizado(Boolean autorizado) {
        this.autorizado = autorizado;
    }

    public Boolean getPrimeraVez() {
        return primeraVez;
    }

    public void setPrimeraVez(Boolean primeraVez) {
        this.primeraVez = primeraVez;
    }

    public List<Horario> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<Horario> horarios) {
        this.horarios = horarios;
    }

}