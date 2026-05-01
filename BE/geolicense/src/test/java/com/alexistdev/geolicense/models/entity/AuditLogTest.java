/*
 * Copyright (c) 2026.
 * Project: Geolicense
 * Author: Alexsander Hendra Wijaya
 * Github: https://github.com/alexistdev
 * Email: alexistdev@gmail.com
 */

package com.alexistdev.geolicense.models.entity;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuditLogTest {

    @Test
    @Order(1)
    @DisplayName("1. Test AuditLog getters and setters")
    void testAuditLogGettersAndSetters() {
        AuditLog auditLog = new AuditLog();
        UUID id = UUID.randomUUID();
        UUID licenseId = UUID.randomUUID();
        String machineId = "test-machine-id";
        AuditLogAction action = AuditLogAction.ACTIVATED;
        String reason = "Test reason";
        String ipAddress = "127.0.0.1";
        LocalDateTime timestamp = LocalDateTime.now();

        auditLog.setId(id);
        auditLog.setLicenseId(licenseId);
        auditLog.setMachineId(machineId);
        auditLog.setAction(action);
        auditLog.setReason(reason);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTimestamp(timestamp);

        Assertions.assertEquals(id, auditLog.getId());
        Assertions.assertEquals(licenseId, auditLog.getLicenseId());
        Assertions.assertEquals(machineId, auditLog.getMachineId());
        Assertions.assertEquals(action, auditLog.getAction());
        Assertions.assertEquals(reason, auditLog.getReason());
        Assertions.assertEquals(ipAddress, auditLog.getIpAddress());
        Assertions.assertEquals(timestamp, auditLog.getTimestamp());
    }

    @Test
    @Order(2)
    @DisplayName("2. Test AuditLog equals and hashCode")
    void testEqualsAndHashCode() {
        AuditLog auditLog1 = new AuditLog();
        UUID id = UUID.randomUUID();
        auditLog1.setId(id);

        AuditLog auditLog2 = new AuditLog();
        auditLog2.setId(id);

        AuditLog auditLog3 = new AuditLog();
        auditLog3.setId(UUID.randomUUID());

        Assertions.assertEquals(auditLog1, auditLog2);
        Assertions.assertNotEquals(auditLog1, auditLog3);
        Assertions.assertNotEquals(null, auditLog1);
        Assertions.assertNotEquals(new Object(), auditLog1);

        Assertions.assertEquals(auditLog1.hashCode(), auditLog2.hashCode());
        Assertions.assertNotEquals(auditLog1.hashCode(), auditLog3.hashCode());
    }

    @Test
    @Order(3)
    @DisplayName("3. Test AuditLog equals with different object")
    void testEqualsWithDifferentObject() {
        AuditLog auditLog = new AuditLog();
        auditLog.setId(UUID.randomUUID());

        Product product = new Product();
        product.setId(UUID.randomUUID());

        Assertions.assertNotEquals(auditLog, product);
    }
}
