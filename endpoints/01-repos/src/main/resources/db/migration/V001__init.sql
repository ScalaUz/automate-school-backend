CREATE TYPE SUBJECT_CATEGORY AS ENUM (
    'Aniq fanlar',
    'Tabiiy va iqtisodiy fanlar',
    'Filologiya',
    'Ijtimoiy fanlar',
    'Amaliy fanlar'
);

CREATE TABLE IF NOT EXISTS users(
  id UUID PRIMARY KEY NOT NULL,
  name VARCHAR NOT NULL,
  email VARCHAR NOT NULL UNIQUE,
  password VARCHAR NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE,
  deleted_at TIMESTAMP WITH TIME ZONE
);

INSERT INTO
  users (id, created_at, name, email, password)
VALUES
  (
    '72a911c8-ad24-4e2d-8930-9c3ba51741df',
    '2023-06-30T16:02:51+05:00',
    'admin',
    'admin@scala.uz',
    '$s0$e0801$5JK3Ogs35C2h5htbXQoeEQ==$N7HgNieSnOajn1FuEB7l4PhC6puBSq+e1E8WUaSJcGY='
  );


Create TABLE IF NOT EXISTS groups (
  id UUID PRIMARY KEY NOT NULL,
  name VARCHAR NOT NULL,
  level INT NOT NULL,
  students INT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE,
  deleted_at TIMESTAMP WITH TIME ZONE,
  UNIQUE(level, name)
);

CREATE TABLE IF NOT EXISTS teachers (
  id UUID PRIMARY KEY NOT NULL,
  name VARCHAR NOT NULL,
  workload INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  updated_at TIMESTAMP WITH TIME ZONE,
  deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS subjects (
  id UUID PRIMARY KEY NOT NULL,
  name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS study_hours(
  subject_id UUID NOT NULL
    CONSTRAINT fk_subjects REFERENCES subjects (id) ON UPDATE CASCADE ON DELETE CASCADE,
  level INT NOT NULL,
  hour INT NOT NULL,
  UNIQUE(level, subject_id)
);

CREATE TABLE IF NOT EXISTS teachers_subjects (
  teacher_id UUID NOT NULL
    CONSTRAINT fk_teachers_subjects_teachers REFERENCES teachers (id) ON UPDATE CASCADE ON DELETE CASCADE,
  subject_id UUID NOT NULL
    CONSTRAINT fk_teachers_subjects_subjects REFERENCES subjects (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS timetable(
  group_id UUID NOT NULL
    CONSTRAINT fk_groups REFERENCES groups (id) ON UPDATE CASCADE ON DELETE CASCADE,
  teacher_id UUID NOT NULL
    CONSTRAINT fk_teachers REFERENCES teachers (id) ON UPDATE CASCADE ON DELETE CASCADE,
  subject_id UUID NOT NULL
    CONSTRAINT fk_subjects REFERENCES subjects (id) ON UPDATE CASCADE ON DELETE CASCADE,
  weekday VARCHAR NOT NULL,
  moment INT NOT NULL,
  UNIQUE(teacher_id, weekday, moment)
);
