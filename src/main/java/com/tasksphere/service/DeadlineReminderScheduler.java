package com.tasksphere.service;

import com.tasksphere.model.Team;
import com.tasksphere.model.TeamStatus;
import com.tasksphere.repository.ReminderRepository;
import com.tasksphere.repository.TeamRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DeadlineReminderScheduler {

    private final TeamRepository teamRepository;
    private final EmailService emailService;

    // 🔥 Constructor
    private final ReminderRepository reminderRepository;

    public DeadlineReminderScheduler(TeamRepository teamRepository,
                                     EmailService emailService,
                                     ReminderRepository reminderRepository) {
        this.teamRepository = teamRepository;
        this.emailService = emailService;
        this.reminderRepository = reminderRepository;
    }

    //  Scheduler (9 O'clock )
    @Scheduled(cron = "0 0 18 * * *")
    public void sendReminder() {

        List<Team> teams = teamRepository.findAll();

        int reminderDays = reminderRepository.getReminderDays(); //  सबके लिए same

        LocalDate today = LocalDate.now();

        for (Team team : teams) {

            // null check (important)
            if (team.deadline() == null) continue;

            // अगर task complete नहीं है
            if (team.status() != TeamStatus.COMPLETED && team.deadline() != null) {

                LocalDate reminderDate = team.deadline().minusDays(reminderDays);

                if (!team.reminderSent() && !today.isBefore(reminderDate)) {

                    List<String> emails = teamRepository.getStudentEmailsByTeamId(team.teamId());
                    if (emails == null || emails.isEmpty()) continue;

                    for (String email : emails) {

                        emailService.send(
                                email,
                                "Task Reminder - TaskSphere",
                                "Hello,\n\n"
                                        + "Reminder for your task:\n\n"
                                        + "Team: " + team.teamName() + "\n"
                                        + "Task: " + team.taskName() + "\n"
                                        + "Deadline: " + team.deadline() + "\n\n"
                                        + "Complete it on time.\n\n"
                                        + "TaskSphere"
                        );

                        System.out.println("Sent to: " + email);
                    }

                    //  mark sent
                    teamRepository.markReminderSent(team.teamId());
                }
            }
        }
    }
}