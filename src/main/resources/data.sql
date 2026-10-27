INSERT INTO categorias (nombre, descripcion) VALUES ('Suculentas', 'Plantas que almacenan agua, ideales para principiantes');
INSERT INTO categorias (nombre, descripcion) VALUES ('Interior', 'Plantas que crecen bien dentro de casa');
INSERT INTO categorias (nombre, descripcion) VALUES ('Aromaticas', 'Hierbas para cocinar y perfumar el jardin');

INSERT INTO plantas (nombre, nombre_cientifico, precio, stock, nivel_luz, riego_cada_dias, categoria_id, version) VALUES ('Aloe Vera', 'Aloe barbadensis', 8.50, 20, 'SOL', 14, 1, 0);
INSERT INTO plantas (nombre, nombre_cientifico, precio, stock, nivel_luz, riego_cada_dias, categoria_id, version) VALUES ('Echeveria', 'Echeveria elegans', 5.00, 35, 'SOL', 10, 1, 0);
INSERT INTO plantas (nombre, nombre_cientifico, precio, stock, nivel_luz, riego_cada_dias, categoria_id, version) VALUES ('Monstera', 'Monstera deliciosa', 25.90, 8, 'MEDIA', 7, 2, 0);
INSERT INTO plantas (nombre, nombre_cientifico, precio, stock, nivel_luz, riego_cada_dias, categoria_id, version) VALUES ('Potus', 'Epipremnum aureum', 9.99, 15, 'SOMBRA', 5, 2, 0);
INSERT INTO plantas (nombre, nombre_cientifico, precio, stock, nivel_luz, riego_cada_dias, categoria_id, version) VALUES ('Lavanda', 'Lavandula angustifolia', 6.75, 25, 'SOL', 3, 3, 0);
INSERT INTO plantas (nombre, nombre_cientifico, precio, stock, nivel_luz, riego_cada_dias, categoria_id, version) VALUES ('Albahaca', 'Ocimum basilicum', 3.20, 40, 'SOL', 2, 3, 0);

INSERT INTO clientes (nombre, apellido, email, telefono, direccion) VALUES ('Lucia', 'Ramirez', 'lucia@correo.com', '600111222', 'Calle Rosal 12');
INSERT INTO clientes (nombre, apellido, email, telefono, direccion) VALUES ('Mateo', 'Fernandez', 'mateo@correo.com', '600333444', 'Av. Los Pinos 45');

INSERT INTO pedidos (fecha, estado, total, cliente_id) VALUES ('2026-10-20', 'ENTREGADO', 27.00, 1);
INSERT INTO detalle_pedidos (cantidad, precio_unitario, subtotal, pedido_id, planta_id) VALUES (2, 8.50, 17.00, 1, 1);
INSERT INTO detalle_pedidos (cantidad, precio_unitario, subtotal, pedido_id, planta_id) VALUES (2, 5.00, 10.00, 1, 2);
