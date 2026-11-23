-- Drop existing views, triggers, and tables
DROP VIEW IF EXISTS booking_summary_view;
DROP VIEW IF EXISTS available_trains_view;
DROP TABLE IF EXISTS Cancellation CASCADE;
DROP TABLE IF EXISTS Booking CASCADE;
DROP TABLE IF EXISTS Schedule CASCADE;
DROP TABLE IF EXISTS Train CASCADE;
DROP TABLE IF EXISTS Station CASCADE;
DROP TABLE IF EXISTS Users CASCADE;
DROP FUNCTION IF EXISTS restore_seats_func CASCADE;

-- Users table
CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(15) NOT NULL DEFAULT 'Passenger'
);

-- Station table
CREATE TABLE Station (
    station_id SERIAL PRIMARY KEY,
    station_code VARCHAR(10) NOT NULL UNIQUE,
    station_name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL
);

-- Train table
CREATE TABLE Train (
    train_id SERIAL PRIMARY KEY,
    train_number VARCHAR(10) NOT NULL UNIQUE,
    train_name VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL,
    train_type VARCHAR(30) NOT NULL DEFAULT 'Express'
);

-- Schedule table
CREATE TABLE Schedule (
    schedule_id SERIAL PRIMARY KEY,
    train_id INT NOT NULL REFERENCES Train(train_id) ON DELETE CASCADE,
    source_station_id INT NOT NULL REFERENCES Station(station_id),
    destination_station_id INT NOT NULL REFERENCES Station(station_id),
    departure_date DATE NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    available_seats INT NOT NULL,
    fare DECIMAL(8, 2) NOT NULL
);

-- Booking table
CREATE TABLE Booking (
    booking_id SERIAL PRIMARY KEY,
    pnr_number VARCHAR(20) NOT NULL UNIQUE,
    user_id INT NOT NULL REFERENCES Users(user_id) ON DELETE CASCADE,
    schedule_id INT NOT NULL REFERENCES Schedule(schedule_id),
    passenger_name VARCHAR(100) NOT NULL,
    passenger_age INT NOT NULL,
    passenger_gender VARCHAR(10) NOT NULL,
    seats_booked INT NOT NULL DEFAULT 1,
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    booking_status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
);

-- Cancellation table
CREATE TABLE Cancellation (
    cancellation_id SERIAL PRIMARY KEY,
    booking_id INT NOT NULL UNIQUE REFERENCES Booking(booking_id) ON DELETE CASCADE,
    cancellation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(255) DEFAULT 'User cancelled'
);

-- Function and trigger to restore seats when cancelled
CREATE OR REPLACE FUNCTION restore_seats_func()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Schedule s
    SET available_seats = s.available_seats + b.seats_booked
    FROM Booking b
    WHERE s.schedule_id = b.schedule_id AND b.booking_id = NEW.booking_id;

    UPDATE Booking SET booking_status = 'CANCELLED' WHERE booking_id = NEW.booking_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_restore_seats
AFTER INSERT ON Cancellation
FOR EACH ROW
EXECUTE FUNCTION restore_seats_func();

-- View 1: Available trains view
CREATE VIEW available_trains_view AS
SELECT 
    s.schedule_id,
    t.train_number,
    t.train_name,
    src.station_name AS source_station,
    dst.station_name AS destination_station,
    s.departure_date,
    s.departure_time,
    s.available_seats,
    s.fare
FROM Schedule s
JOIN Train t ON s.train_id = t.train_id
JOIN Station src ON s.source_station_id = src.station_id
JOIN Station dst ON s.destination_station_id = dst.station_id;

-- View 2: Booking summary view
CREATE VIEW booking_summary_view AS
SELECT 
    b.booking_id,
    b.pnr_number,
    b.user_id,
    u.username,
    b.passenger_name,
    t.train_number,
    t.train_name,
    src.station_name AS source_station,
    dst.station_name AS destination_station,
    s.departure_date,
    b.seats_booked,
    (b.seats_booked * s.fare) AS total_fare,
    b.booking_status
FROM Booking b
JOIN Users u ON b.user_id = u.user_id
JOIN Schedule s ON b.schedule_id = s.schedule_id
JOIN Train t ON s.train_id = t.train_id
JOIN Station src ON s.source_station_id = src.station_id
JOIN Station dst ON s.destination_station_id = dst.station_id;

-- Sample data
INSERT INTO Users (username, password, full_name, age, gender, phone, email, role) VALUES
('admin', 'admin123', 'Admin User', 30, 'Male', '9999999990', 'admin@railway.com', 'Admin'),
('john', 'pass123', 'John Doe', 25, 'Male', '9999999991', 'john@gmail.com', 'Passenger'),
('jane', 'pass123', 'Jane Smith', 24, 'Female', '9999999992', 'jane@gmail.com', 'Passenger');

INSERT INTO Station (station_code, station_name, city) VALUES
('NDLS', 'New Delhi', 'Delhi'),
('CSTM', 'Mumbai CSMT', 'Mumbai'),
('HWH', 'Howrah', 'Kolkata'),
('MAS', 'Chennai Central', 'Chennai'),
('SBC', 'Bengaluru City', 'Bengaluru');

INSERT INTO Train (train_number, train_name, total_seats, train_type) VALUES
('12952', 'Rajdhani Express', 100, 'Superfast'),
('12002', 'Shatabdi Express', 80, 'Shatabdi'),
('12840', 'Howrah Mail', 120, 'Mail'),
('22692', 'Bengaluru Express', 90, 'Express'),
('20901', 'Vande Bharat', 60, 'Superfast');

INSERT INTO Schedule (train_id, source_station_id, destination_station_id, departure_date, departure_time, arrival_time, available_seats, fare) VALUES
(1, 1, 2, '2026-11-20', '16:55:00', '08:35:00', 98, 2150.00),
(2, 1, 5, '2026-11-21', '06:00:00', '14:00:00', 80, 1450.00),
(3, 4, 3, '2026-11-22', '23:45:00', '03:50:00', 120, 1850.00),
(4, 5, 1, '2026-11-23', '20:00:00', '05:55:00', 90, 2300.00),
(5, 2, 1, '2026-11-24', '06:00:00', '12:25:00', 60, 1950.00);

INSERT INTO Booking (pnr_number, user_id, schedule_id, passenger_name, passenger_age, passenger_gender, seats_booked, booking_status) VALUES
('PNR101', 2, 1, 'John Doe', 25, 'Male', 2, 'CONFIRMED');
