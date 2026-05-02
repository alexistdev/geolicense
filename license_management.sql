-- ============================================================
--  License Management System
--  Database: MySQL 8.0+
--  Generated: 2026-04-08
-- ============================================================

CREATE DATABASE IF NOT EXISTS license_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE license_management;

-- ============================================================
--  1. USERS
--     Menyimpan data pelanggan dan admin
-- ============================================================
CREATE TABLE users (
    id               CHAR(36)        NOT NULL DEFAULT (UUID()),
    name             VARCHAR(150)    NOT NULL,
    email            VARCHAR(255)    NOT NULL,
    password_hash    VARCHAR(255)    NOT NULL,
    company_name     VARCHAR(200)    NULL,
    role             ENUM('admin', 'customer') NOT NULL DEFAULT 'customer',
    is_active        TINYINT(1)      NOT NULL DEFAULT 1,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  2. PRODUCTS
--     Katalog software / produk yang dijual
-- ============================================================
CREATE TABLE products (
    id               CHAR(36)        NOT NULL DEFAULT (UUID()),
    name             VARCHAR(200)    NOT NULL,
    slug             VARCHAR(200)    NOT NULL,
    description      TEXT            NULL,
    license_type     ENUM('perpetual', 'subscription', 'trial') NOT NULL DEFAULT 'perpetual',
    max_activations  INT             NOT NULL DEFAULT 1
                         COMMENT 'Jumlah maksimal perangkat yang boleh aktivasi 1 lisensi',
    validity_days    INT             NULL
                         COMMENT 'NULL = tidak ada batas waktu (perpetual)',
    is_active        TINYINT(1)      NOT NULL DEFAULT 1,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_products_slug (slug),
    INDEX idx_products_license_type (license_type),
    INDEX idx_products_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  3. ORDERS
--     Transaksi pembelian lisensi
-- ============================================================
CREATE TABLE orders (
    id               CHAR(36)        NOT NULL DEFAULT (UUID()),
    user_id          CHAR(36)        NOT NULL,
    product_id       CHAR(36)        NOT NULL,
    invoice_number   VARCHAR(100)    NOT NULL,
    amount           DECIMAL(15, 2)  NOT NULL DEFAULT 0.00,
    status           ENUM('pending', 'paid', 'cancelled', 'refunded') NOT NULL DEFAULT 'pending',
    paid_at          DATETIME        NULL,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_orders_invoice (invoice_number),
    INDEX idx_orders_user_id (user_id),
    INDEX idx_orders_product_id (product_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created_at (created_at),

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_orders_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  4. LICENSES
--     Lisensi yang dimiliki user, dibuat setelah order paid
-- ============================================================
CREATE TABLE licenses (
    id               CHAR(36)        NOT NULL DEFAULT (UUID()),
    user_id          CHAR(36)        NOT NULL,
    product_id       CHAR(36)        NOT NULL,
    order_id         CHAR(36)        NOT NULL,
    license_key      VARCHAR(100)    NOT NULL
                         COMMENT 'Format: XXXXX-XXXXX-XXXXX-XXXXX',
    status           ENUM('active', 'expired', 'suspended', 'revoked') NOT NULL DEFAULT 'active',
    max_activations  INT             NOT NULL DEFAULT 1,
    activation_count INT             NOT NULL DEFAULT 0,
    expires_at       DATETIME        NULL
                         COMMENT 'NULL = lisensi perpetual / tidak ada expired',
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_licenses_key (license_key),
    INDEX idx_licenses_user_id (user_id),
    INDEX idx_licenses_product_id (product_id),
    INDEX idx_licenses_order_id (order_id),
    INDEX idx_licenses_status (status),
    INDEX idx_licenses_expires_at (expires_at),

    CONSTRAINT fk_licenses_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_licenses_product
        FOREIGN KEY (product_id) REFERENCES products (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,

    CONSTRAINT fk_licenses_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  5. ACTIVATIONS
--     Setiap perangkat yang mengaktifkan lisensi
-- ============================================================
CREATE TABLE activations (
    id               CHAR(36)        NOT NULL DEFAULT (UUID()),
    license_id       CHAR(36)        NOT NULL,
    hardware_id      VARCHAR(255)    NOT NULL
                         COMMENT 'Fingerprint perangkat: hash dari CPU+MAC+Disk serial',
    hostname         VARCHAR(255)    NULL,
    ip_address       VARCHAR(45)     NULL
                         COMMENT 'Mendukung IPv4 dan IPv6',
    os_info          VARCHAR(255)    NULL
                         COMMENT 'Contoh: Windows 11 Pro 22H2',
    app_version      VARCHAR(50)     NULL,
    is_active        TINYINT(1)      NOT NULL DEFAULT 1,
    activated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_seen_at     DATETIME        NULL
                         COMMENT 'Diupdate setiap validasi berhasil',
    deactivated_at   DATETIME        NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_activations_license_hardware (license_id, hardware_id),
    INDEX idx_activations_license_id (license_id),
    INDEX idx_activations_hardware_id (hardware_id),
    INDEX idx_activations_is_active (is_active),

    CONSTRAINT fk_activations_license
        FOREIGN KEY (license_id) REFERENCES licenses (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  6. API_VALIDATIONS
--     Log setiap request validasi dari software client
-- ============================================================
CREATE TABLE api_validations (
    id               CHAR(36)        NOT NULL DEFAULT (UUID()),
    license_id       CHAR(36)        NOT NULL,
    activation_id    CHAR(36)        NULL
                         COMMENT 'NULL jika validasi gagal sebelum activation ditemukan',
    hardware_id      VARCHAR(255)    NOT NULL,
    ip_address       VARCHAR(45)     NULL,
    result           ENUM('success', 'failed', 'expired', 'revoked', 'max_reached') NOT NULL,
    failure_reason   VARCHAR(500)    NULL,
    validated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_api_val_license_id (license_id),
    INDEX idx_api_val_activation_id (activation_id),
    INDEX idx_api_val_result (result),
    INDEX idx_api_val_validated_at (validated_at),
    INDEX idx_api_val_hardware_id (hardware_id),

    CONSTRAINT fk_api_val_license
        FOREIGN KEY (license_id) REFERENCES licenses (id)
        ON UPDATE CASCADE ON DELETE CASCADE,

    CONSTRAINT fk_api_val_activation
        FOREIGN KEY (activation_id) REFERENCES activations (id)
        ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  7. API_KEYS
--     Kunci autentikasi untuk server-to-server / dashboard
-- ============================================================
CREATE TABLE api_keys (
    id               CHAR(36)        NOT NULL DEFAULT (UUID()),
    user_id          CHAR(36)        NOT NULL,
    key_hash         VARCHAR(255)    NOT NULL
                         COMMENT 'Hash bcrypt/SHA-256 dari raw API key',
    name             VARCHAR(150)    NOT NULL
                         COMMENT 'Label deskriptif, contoh: "Production App v2"',
    allowed_ips      JSON            NULL
                         COMMENT 'Array of IP/CIDR. NULL = allow all. Contoh: ["192.168.1.0/24"]',
    is_active        TINYINT(1)      NOT NULL DEFAULT 1,
    expires_at       DATETIME        NULL,
    last_used_at     DATETIME        NULL,
    created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_api_keys_hash (key_hash),
    INDEX idx_api_keys_user_id (user_id),
    INDEX idx_api_keys_is_active (is_active),

    CONSTRAINT fk_api_keys_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================
--  STORED PROCEDURE: Validasi Lisensi via API
--  Dipanggil oleh backend saat client software request validasi
--
--  Parameter:
--    p_license_key  : license key yang dikirim client
--    p_hardware_id  : fingerprint perangkat
--    p_ip_address   : IP client
--    p_app_version  : versi aplikasi client
--
--  Return: result_code + result_message
-- ============================================================
DELIMITER $$

CREATE PROCEDURE sp_validate_license(
    IN  p_license_key   VARCHAR(100),
    IN  p_hardware_id   VARCHAR(255),
    IN  p_ip_address    VARCHAR(45),
    IN  p_app_version   VARCHAR(50),
    OUT p_result        VARCHAR(50),
    OUT p_message       VARCHAR(500)
)
BEGIN
    DECLARE v_license_id       CHAR(36);
    DECLARE v_status           VARCHAR(20);
    DECLARE v_expires_at       DATETIME;
    DECLARE v_max_act          INT;
    DECLARE v_act_count        INT;
    DECLARE v_activation_id    CHAR(36);
    DECLARE v_act_is_active    TINYINT(1);

    -- 1. Cari lisensi berdasarkan license_key
    SELECT id, status, expires_at, max_activations, activation_count
    INTO   v_license_id, v_status, v_expires_at, v_max_act, v_act_count
    FROM   licenses
    WHERE  license_key = p_license_key
    LIMIT  1;

    IF v_license_id IS NULL THEN
        SET p_result = 'failed';
        SET p_message = 'License key tidak ditemukan.';
        INSERT INTO api_validations (id, license_id, hardware_id, ip_address, result, failure_reason)
            VALUES (UUID(), '00000000-0000-0000-0000-000000000000',
                    p_hardware_id, p_ip_address, 'failed', p_message);
        LEAVE sp_validate_license;
    END IF;

    -- 2. Cek status lisensi
    IF v_status = 'revoked' THEN
        SET p_result = 'revoked';
        SET p_message = 'Lisensi telah dicabut (revoked).';
    ELSEIF v_status = 'suspended' THEN
        SET p_result = 'failed';
        SET p_message = 'Lisensi sedang ditangguhkan.';
    ELSEIF v_status = 'expired' OR (v_expires_at IS NOT NULL AND v_expires_at < NOW()) THEN
        SET p_result = 'expired';
        SET p_message = 'Lisensi telah kedaluwarsa.';
        -- Pastikan status diupdate ke expired
        UPDATE licenses SET status = 'expired' WHERE id = v_license_id;
    ELSE
        -- 3. Cek apakah hardware ini sudah pernah diaktifkan
        SELECT id, is_active
        INTO   v_activation_id, v_act_is_active
        FROM   activations
        WHERE  license_id = v_license_id AND hardware_id = p_hardware_id
        LIMIT  1;

        IF v_activation_id IS NOT NULL THEN
            -- Hardware sudah dikenal → update last_seen_at
            IF v_act_is_active = 0 THEN
                -- Reaktivasi perangkat yang pernah di-deactivate
                UPDATE activations
                SET    is_active = 1,
                       deactivated_at = NULL,
                       last_seen_at = NOW(),
                       app_version = p_app_version
                WHERE  id = v_activation_id;
            ELSE
                UPDATE activations
                SET    last_seen_at = NOW(),
                       app_version  = p_app_version
                WHERE  id = v_activation_id;
            END IF;

            SET p_result  = 'success';
            SET p_message = 'Lisensi valid.';
        ELSE
            -- 4. Hardware baru → cek apakah masih ada slot aktivasi
            IF v_act_count >= v_max_act THEN
                SET p_result  = 'max_reached';
                SET p_message = CONCAT('Batas maksimal aktivasi (', v_max_act, ' perangkat) telah tercapai.');
            ELSE
                -- Buat aktivasi baru
                SET v_activation_id = UUID();
                INSERT INTO activations
                    (id, license_id, hardware_id, ip_address, app_version, is_active, activated_at, last_seen_at)
                VALUES
                    (v_activation_id, v_license_id, p_hardware_id, p_ip_address, p_app_version, 1, NOW(), NOW());

                -- Increment counter
                UPDATE licenses
                SET    activation_count = activation_count + 1
                WHERE  id = v_license_id;

                SET p_result  = 'success';
                SET p_message = 'Aktivasi berhasil. Lisensi valid.';
            END IF;
        END IF;
    END IF;

    -- 5. Catat log validasi
    INSERT INTO api_validations
        (id, license_id, activation_id, hardware_id, ip_address, result, failure_reason)
    VALUES
        (UUID(), v_license_id, v_activation_id,
         p_hardware_id, p_ip_address,
         p_result,
         IF(p_result = 'success', NULL, p_message));

END$$

DELIMITER ;


-- ============================================================
--  SEED DATA — contoh data awal untuk testing
-- ============================================================

-- Admin user (password: Admin@1234 — ganti hash di production!)
INSERT INTO users (id, name, email, password_hash, role) VALUES
    ('11111111-0000-0000-0000-000000000001',
     'Super Admin', 'admin@licenseapp.com',
     '$2y$10$examplehashforsuperadmin000000000000000000000000000000',
     'admin');

-- Contoh produk
INSERT INTO products (id, name, slug, description, license_type, max_activations, validity_days) VALUES
    ('22222222-0000-0000-0000-000000000001',
     'MyApp Pro', 'myapp-pro',
     'Versi profesional MyApp dengan fitur lengkap.',
     'perpetual', 2, NULL),
    ('22222222-0000-0000-0000-000000000002',
     'MyApp Business', 'myapp-business',
     'Versi bisnis MyApp dengan dukungan prioritas.',
     'subscription', 5, 365);

-- ============================================================
--  CONTOH QUERY BERGUNA
-- ============================================================

-- Lihat semua lisensi aktif beserta info user dan produk
SELECT
    l.license_key,
    u.name        AS user_name,
    u.email,
    p.name        AS product_name,
    l.status,
    l.activation_count,
    l.max_activations,
    l.expires_at,
    l.created_at
FROM licenses l
JOIN users    u ON u.id = l.user_id
JOIN products p ON p.id = l.product_id
WHERE l.status = 'active'
ORDER BY l.created_at DESC;

-- Lihat perangkat yang aktif untuk suatu license key
SELECT
    a.hardware_id,
    a.hostname,
    a.ip_address,
    a.os_info,
    a.app_version,
    a.activated_at,
    a.last_seen_at
FROM activations a
JOIN licenses l ON l.id = a.license_id
WHERE l.license_key = 'XXXXX-XXXXX-XXXXX-XXXXX'
  AND a.is_active = 1;

-- Log validasi 7 hari terakhir per lisensi
SELECT
    l.license_key,
    v.result,
    COUNT(*) AS total,
    MAX(v.validated_at) AS last_attempt
FROM api_validations v
JOIN licenses l ON l.id = v.license_id
WHERE v.validated_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
GROUP BY l.license_key, v.result
ORDER BY l.license_key, v.result;

-- Panggil stored procedure validasi (untuk testing manual)
-- CALL sp_validate_license('LICENSE-KEY-DISINI', 'HARDWARE-ID', '127.0.0.1', '1.0.0', @res, @msg);
-- SELECT @res AS result, @msg AS message;
