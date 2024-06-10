package com.bowlingpoints.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "persona", schema = "public")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Integer idPersona;

    @Column(name = "primer_nombre", nullable = false)
    private String primerNombre;

    @Column(name = "segund_nombre", nullable = false)
    private String segundoNombre;

    @Column(name = "primer_apellido", nullable = false)
    private String primerApellido;

    @Column(name = "segundo_apellido", nullable = false)
    private String segundoApellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fechaNacimiento;

    @Column(name = "celular", nullable = false)
    private String celular;

    @Column(name = "correo_electronico", nullable = false)
    private String correoElectronico;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
    }
