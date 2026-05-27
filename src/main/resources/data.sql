insert into super_admins (
    name,
    email,
    password_hash,
    contact_number
)
select
    'TaskSphere Admin',
    'admin@tasksphere.com',
    '$2a$10$4sR4iK0iM0RKZ77N8D66vOhKcbGaVbL50DJBSSTXobHxPPH/FkN6O',
    '9000000000'
    where not exists (
    select 1 from super_admins
);

INSERT INTO reminder_settings (id, reminder_days)
SELECT 1, 2
    WHERE NOT EXISTS (SELECT 1 FROM reminder_settings);