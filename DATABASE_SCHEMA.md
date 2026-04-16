# VMS (Volunteer Management System) - Database Schema

## Overview

This document describes the PostgreSQL database structure for the Volunteer Management System. The system manages volunteers, events, and tracks volunteering hours.

---

## Database Tables

### 1. Volunteers Table

Stores information about volunteers.

```sql
CREATE TABLE volunteers (
    volunteer_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**

- `volunteer_id` (VARCHAR, PK): Unique identifier for each volunteer
- `name` (VARCHAR): Name of the volunteer
- `created_at` (TIMESTAMP): Record creation timestamp
- `updated_at` (TIMESTAMP): Record last update timestamp

---

### 2. Events Table

Stores information about volunteer events.

```sql
CREATE TABLE events (
    event_id VARCHAR(50) PRIMARY KEY,
    event_name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Columns:**

- `event_id` (VARCHAR, PK): Unique identifier for each event
- `event_name` (VARCHAR): Name of the event
- `date` (DATE): Date when the event occurs
- `created_at` (TIMESTAMP): Record creation timestamp
- `updated_at` (TIMESTAMP): Record last update timestamp

---

### 3. Event Volunteers Table (Junction/Bridge Table)

Tracks the relationship between volunteers and events, including hours logged.

```sql
CREATE TABLE event_volunteers (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(50) NOT NULL,
    volunteer_id VARCHAR(50) NOT NULL,
    hours INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    CONSTRAINT fk_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers(volunteer_id) ON DELETE CASCADE,
    CONSTRAINT unique_event_volunteer UNIQUE(event_id, volunteer_id)
);
```

**Columns:**

- `id` (BIGSERIAL, PK): Auto-generated unique identifier
- `event_id` (VARCHAR, FK): References events table
- `volunteer_id` (VARCHAR, FK): References volunteers table
- `hours` (INT): Number of hours volunteered (default: 0)
- `created_at` (TIMESTAMP): Record creation timestamp
- `updated_at` (TIMESTAMP): Record last update timestamp

**Constraints:**

- Foreign Key to `events` table with CASCADE delete
- Foreign Key to `volunteers` table with CASCADE delete
- UNIQUE constraint on (event_id, volunteer_id) to prevent duplicate enrollments

---

## Indexes (Performance Optimization)

```sql
-- Index for faster volunteer lookups by name
CREATE INDEX idx_volunteer_name ON volunteers(name);

-- Index for faster event lookups by name
CREATE INDEX idx_event_name ON events(event_name);

-- Index for faster event lookups by date
CREATE INDEX idx_event_date ON events(date);

-- Indexes on foreign keys for faster joins
CREATE INDEX idx_event_volunteers_event_id ON event_volunteers(event_id);
CREATE INDEX idx_event_volunteers_volunteer_id ON event_volunteers(volunteer_id);
```

---

## Entity Relationships

```
Volunteers (1) ──── (M) Event_Volunteers ──── (M) Events
```

- One volunteer can enroll in multiple events
- One event can have multiple volunteers
- The `event_volunteers` table tracks the many-to-many relationship and stores hours logged

---

## Complete Schema Creation Script

```sql
-- Create volunteers table
CREATE TABLE volunteers (
    volunteer_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create events table
CREATE TABLE events (
    event_id VARCHAR(50) PRIMARY KEY,
    event_name VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create event_volunteers junction table
CREATE TABLE event_volunteers (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(50) NOT NULL,
    volunteer_id VARCHAR(50) NOT NULL,
    hours INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    CONSTRAINT fk_volunteer FOREIGN KEY (volunteer_id) REFERENCES volunteers(volunteer_id) ON DELETE CASCADE,
    CONSTRAINT unique_event_volunteer UNIQUE(event_id, volunteer_id)
);

-- Create indexes for performance
CREATE INDEX idx_volunteer_name ON volunteers(name);
CREATE INDEX idx_event_name ON events(event_name);
CREATE INDEX idx_event_date ON events(date);
CREATE INDEX idx_event_volunteers_event_id ON event_volunteers(event_id);
CREATE INDEX idx_event_volunteers_volunteer_id ON event_volunteers(volunteer_id);
```

---

## Supabase Setup Instructions

### Option 1: Using Supabase SQL Editor (Recommended for beginners)

1. **Login to Supabase**: https://supabase.com/
2. **Open your project** and go to **SQL Editor**
3. **Create a new query** by clicking "New Query"
4. **Copy the complete schema creation script above**
5. **Paste it** into the SQL editor
6. **Click "Run"** to execute all statements

### Option 2: Using Supabase CLI (Recommended for developers)

**Prerequisites:**

```bash
# Install Supabase CLI
npm install -g @supabase/cli
```

**Steps:**

1. **Initialize Supabase project** (if not already done):

```bash
supabase init
```

2. **Login to Supabase**:

```bash
supabase login
```

3. **Link your project**:

```bash
supabase link --project-ref your-project-ref
```

4. **Create migration file**:

```bash
supabase migration new create_vms_tables
```

5. **Add the SQL schema** to the generated migration file in `supabase/migrations/` directory

6. **Deploy migration**:

```bash
supabase db push
```

### Option 3: Using Database Connection (Direct Connection)

If you have PostgreSQL client installed:

```bash
# Install PostgreSQL client (if needed)
# Windows: https://www.postgresql.org/download/windows/
# Mac: brew install postgresql
# Linux: sudo apt-get install postgresql-client

# Connect to Supabase database
psql -h your-project.supabase.co -U postgres -d postgres

# Paste the complete schema creation script
```

---

## Supabase Quick Copy Commands

**For PostgreSQL psql client:**

```bash
# Connection string format (from Supabase project settings > Database > Connection pooling)
postgresql://postgres:[PASSWORD]@[HOST]:[PORT]/postgres

# Example execution:
psql postgresql://postgres:password@project.supabase.co:5432/postgres -f schema.sql
```

---

## Sample Data Insertion Queries

```sql
-- Insert sample volunteers
INSERT INTO volunteers (volunteer_id, name) VALUES
('V001', 'John Doe'),
('V002', 'Jane Smith'),
('V003', 'Mike Johnson');

-- Insert sample events
INSERT INTO events (event_id, event_name, date) VALUES
('E001', 'Beach Cleanup Drive', '2024-05-20'),
('E002', 'Community Kitchen', '2024-06-15'),
('E003', 'Tree Planting Initiative', '2024-07-10');

-- Insert sample event-volunteer relationships
INSERT INTO event_volunteers (event_id, volunteer_id, hours) VALUES
('E001', 'V001', 4),
('E001', 'V002', 5),
('E002', 'V002', 3),
('E002', 'V003', 6),
('E003', 'V001', 2);
```

---

## Useful Queries

### Get volunteer details with event enrollments

```sql
SELECT
    v.volunteer_id,
    v.name,
    COUNT(ev.event_id) as enrolled_events,
    SUM(ev.hours) as total_hours
FROM volunteers v
LEFT JOIN event_volunteers ev ON v.volunteer_id = ev.volunteer_id
GROUP BY v.volunteer_id, v.name;
```

### Get event details with volunteer count

```sql
SELECT
    e.event_id,
    e.event_name,
    e.date,
    COUNT(ev.volunteer_id) as volunteer_count,
    SUM(ev.hours) as total_hours_logged
FROM events e
LEFT JOIN event_volunteers ev ON e.event_id = ev.event_id
GROUP BY e.event_id, e.event_name, e.date;
```

### List all volunteers for a specific event

```sql
SELECT
    v.volunteer_id,
    v.name,
    ev.hours
FROM volunteers v
INNER JOIN event_volunteers ev ON v.volunteer_id = ev.volunteer_id
WHERE ev.event_id = 'E001'
ORDER BY ev.hours DESC;
```

---

## Spring Boot Configuration

Update your `application.properties` to connect to PostgreSQL:

```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/vms_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Supabase Configuration Example
# spring.datasource.url=jdbc:postgresql://[PROJECT-ID].supabase.co:5432/postgres
# spring.datasource.username=postgres
# spring.datasource.password=[YOUR-PASSWORD]

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
```

---

## Data Migration from In-Memory to Database

When migrating from in-memory storage (lists/dicts), follow these steps:

1. **Set up the PostgreSQL/Supabase database** using schema above
2. **Update Spring dependencies** in `pom.xml`:

   ```xml
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <version>42.7.1</version>
   </dependency>
   ```

3. **Update application.properties** with database connection
4. **Ensure JPA repositories are configured** (already present in your code)
5. **Run Spring Boot** - Hibernate will map entities to tables
6. **Migrate existing data** using insert queries above

---

## Notes

- **Primary Keys**: Using VARCHAR for `volunteer_id` and `event_id` as they are business identifiers
- **Auto-generated ID**: The `event_volunteers.id` uses BIGSERIAL for optimal performance
- **Cascade Delete**: Deleting an event or volunteer automatically removes related records
- **Unique Constraint**: Prevents a volunteer from enrolling in the same event twice
- **Timestamps**: Both `created_at` and `updated_at` for audit trail purposes
