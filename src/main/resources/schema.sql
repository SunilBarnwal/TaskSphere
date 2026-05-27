create table if not exists super_admins (
                                            id bigint primary key auto_increment,
                                            name varchar(120) not null,
    email varchar(180) not null unique,
    password_hash varchar(255) not null,
    contact_number varchar(10) not null
    );

create table if not exists teachers (
                                        id bigint primary key auto_increment,
                                        name varchar(120) not null,
    email varchar(180) not null unique,
    password_hash varchar(255) not null,
    contact_number varchar(10) not null,
    first_login_required boolean not null default true
    );

create table if not exists students (
                                        id bigint primary key auto_increment,
                                        name varchar(120) not null,
    enrollment_number varchar(40) not null unique,
    password_hash varchar(255) not null,
    program varchar(160) not null,
    email varchar(180) not null unique,
    contact varchar(10) not null,
    first_login_required boolean not null default true
    );

create table if not exists teams (
                                     team_id bigint primary key auto_increment,
                                     team_name varchar(40) not null unique,
    task_name varchar(180) not null,
    teacher_id bigint not null,
    deadline date not null,
    status varchar(30) not null,
    screenshot_path varchar(255),
    reminder_sent boolean default false,

    teacher_feedback text,
    review_status varchar(30) default 'PENDING_REVIEW',
    reviewed_at timestamp null,

    constraint fk_teams_teacher
    foreign key (teacher_id)
    references teachers(id)
    );

create table if not exists team_members (
                                            id bigint primary key auto_increment,
                                            team_id bigint not null,
                                            student_id bigint not null,
                                            is_leader boolean not null default false,

                                            constraint fk_team_members_team
                                            foreign key (team_id)
    references teams(team_id)
    on delete cascade,

    constraint fk_team_members_student
    foreign key (student_id)
    references students(id),

    constraint uq_team_member unique (team_id, student_id),
    constraint uq_student unique (student_id)
    );

create table if not exists activity_logs (
    id bigint primary key auto_increment,
    team_id bigint not null,
    actor_role varchar(30) not null,
    actor_display_name varchar(180) not null,
    action_type varchar(60) not null,
    description varchar(255) not null,
    created_at timestamp not null default current_timestamp,

    constraint fk_activity_logs_team
    foreign key (team_id)
    references teams(team_id)
    on delete cascade
    );

create table if not exists password_reset_token (
    id bigint auto_increment primary key,
    email varchar(255) not null,
    token varchar(255) not null unique,
    expiry_time datetime not null
    );

create table if not exists reminder_settings (
    id bigint auto_increment primary key,
    reminder_days int
);

create event if not exists delete_expired_tokens
on schedule every 10 minute
do
delete from password_reset_token
where expiry_time < now();