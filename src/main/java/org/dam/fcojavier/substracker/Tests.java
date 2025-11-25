package org.dam.fcojavier.substracker;

import org.dam.fcojavier.substracker.dao.CobroDAO;
import org.dam.fcojavier.substracker.dao.ParticipaDAO;
import org.dam.fcojavier.substracker.dao.SuscripcionDAO;
import org.dam.fcojavier.substracker.dao.UsuarioDAO;
import org.dam.fcojavier.substracker.model.Cobro;
import org.dam.fcojavier.substracker.model.Participa;
import org.dam.fcojavier.substracker.model.Suscripcion;
import org.dam.fcojavier.substracker.model.Usuario;
import org.dam.fcojavier.substracker.model.enums.Categoria;
import org.dam.fcojavier.substracker.model.enums.Ciclo;
import org.dam.fcojavier.substracker.model.enums.MetodoPago;

import java.time.LocalDate;

public class Tests {
    public static void main(String[] args) {
        System.out.println("🚀 INICIANDO PRUEBAS DE INTEGRACIÓN SUBTRACKER");
        System.out.println("=============================================");

        UsuarioDAO usuarioDAO = new UsuarioDAO();
        SuscripcionDAO suscripcionDAO = new SuscripcionDAO();
        ParticipaDAO participaDAO = new ParticipaDAO();
        CobroDAO cobroDAO = new CobroDAO();

        // Variables fuera del try para poder usarlas en el bloque de limpieza si fuera necesario
        Usuario titular = new Usuario();
        Usuario amigo = new Usuario();
        Suscripcion netflix = new Suscripcion();
        Participa participacion = new Participa();
        Cobro cobro = new Cobro();

        try {
            // ----------------------------------------------------------------
            // PASO 1: CREAR USUARIOS
            // ----------------------------------------------------------------
            System.out.println("\n[1] Creando Usuarios...");

            titular.setNombre("Carlos");
            titular.setApellidos("García");
            titular.setEmail("carlos.garcia.test@email.com"); // Email único
            titular.setPassword("123456");

            amigo.setNombre("Lucía");
            amigo.setApellidos("Fernández");
            amigo.setEmail("lucia.fer.test@email.com"); // Email único
            amigo.setPassword("654321");

            if(usuarioDAO.create(titular)) System.out.println("✅ Titular creado ID: " + titular.getId_usuario());
            if(usuarioDAO.create(amigo)) System.out.println("✅ Amigo creado ID: " + amigo.getId_usuario());

            // ----------------------------------------------------------------
            // PASO 2: CREAR SUSCRIPCIÓN
            // ----------------------------------------------------------------
            System.out.println("\n[2] Creando Suscripción...");

            netflix.setNombre("Netflix Test");
            netflix.setPrecio(18.00);
            netflix.setCiclo(Ciclo.MENSUAL);
            netflix.setCategoria(Categoria.OCIO);
            netflix.setActivo(true);
            netflix.setFechaActivacion(LocalDate.now());
            netflix.setFechaRenovacion(LocalDate.now().plusMonths(1));
            netflix.setTitular(titular);

            if(suscripcionDAO.create(netflix)) System.out.println("✅ Suscripción creada ID: " + netflix.getIdSuscripcion());

            // ----------------------------------------------------------------
            // PASO 3: AÑADIR PARTICIPANTE
            // ----------------------------------------------------------------
            System.out.println("\n[3] Añadiendo copagador...");

            participacion.setParticipante(amigo);
            participacion.setSuscripcion(netflix);
            participacion.setCantidadApagar(9.00);
            participacion.setFecha_pagado(LocalDate.now());
            participacion.setMetodo_pago(MetodoPago.BIZUM);
            participacion.setDescripcion("Mitad de Netflix");
            participacion.setPeriodos_cubiertos(1);

            if(participaDAO.create(participacion)) System.out.println("✅ Participación registrada.");

            // ----------------------------------------------------------------
            // PASO 4: REGISTRAR COBRO
            // ----------------------------------------------------------------
            System.out.println("\n[4] Registrando cobro...");

            cobro.setSuscripcion(netflix);
            cobro.setFecha_cobro(LocalDate.now());
            cobro.setMetodo_pago(MetodoPago.TARJETA);
            cobro.setDescripcion("Pago Enero");
            cobro.setPeriodos_cubiertos(1);

            if(cobroDAO.create(cobro)) System.out.println("✅ Cobro registrado ID: " + cobro.getId_cobro());

            // ----------------------------------------------------------------
            // PASO 5: VERIFICACIÓN
            // ----------------------------------------------------------------
            System.out.println("\n[5] Verificando lecturas (JOINs)...");
            Suscripcion s = suscripcionDAO.findById(netflix.getIdSuscripcion());
            System.out.println("   -> Suscripción: " + s.getNombre());
            System.out.println("   -> Titular: " + s.getTitular().getNombre());
            System.out.println("   -> Participantes: " + s.getParticipantes().size());

            // ----------------------------------------------------------------
            // PASO 6: LIMPIEZA DE DATOS (TEARDOWN)
            // ----------------------------------------------------------------
            System.out.println("\n[6] 🧹 LIMPIANDO BASE DE DATOS (TEARDOWN)...");
            System.out.println("----------------------------------------------");

            // 1. Borramos Cobro (Hijo)
            if(cobro.getId_cobro() > 0) {
                if(cobroDAO.delete(cobro.getId_cobro()))
                    System.out.println("🗑️  Cobro eliminado.");
                else
                    System.err.println("⚠️ Error borrando cobro.");
            }

            // 2. Borramos Participa (Hijo - Requiere clave compuesta)
            if(participacion.getParticipante() != null) {
                if(participaDAO.delete(amigo.getId_usuario(), netflix.getIdSuscripcion()))
                    System.out.println("🗑️  Participación eliminada.");
                else
                    System.err.println("⚠️ Error borrando participación.");
            }

            // 3. Borramos Suscripción (Padre intermedio)
            // IMPORTANTE: Si intentáramos borrar el usuario antes que esto, fallaría por la FK.
            if(netflix.getIdSuscripcion() > 0) {
                if(suscripcionDAO.delete(netflix.getIdSuscripcion()))
                    System.out.println("🗑️  Suscripción eliminada.");
                else
                    System.err.println("⚠️ Error borrando suscripción.");
            }

            // 4. Borramos Usuarios (Abuelos)
            if(amigo.getId_usuario() > 0) {
                if(usuarioDAO.delete(amigo.getId_usuario()))
                    System.out.println("🗑️  Usuario (Amigo) eliminado.");
            }
            if(titular.getId_usuario() > 0) {
                if(usuarioDAO.delete(titular.getId_usuario()))
                    System.out.println("🗑️  Usuario (Titular) eliminado.");
            }

            System.out.println("\n✅ PRUEBA COMPLETADA EXITOSAMENTE. BD LIMPIA.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ ERROR EN LA PRUEBA: " + e.getMessage());
        }
    }
}
