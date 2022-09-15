package com.telegrambot.repository;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TelegramBotRepositoryProvider {
    @Getter
    @Setter
    private static AdminRepository adminRepository;

    @Getter
    @Setter
    private static PropertiesRepository propertiesRepository;
    @Getter
    @Setter
    private static LanguageUserRepository languageUserRepository;
    @Getter
    @Setter
    private static ButtonRepository buttonRepository;
    @Getter
    @Setter
    private static MessageRepository messageRepository;
    @Getter
    @Setter
    private static KeyboardRepository keyboardRepository;
    @Getter
    @Setter
    private static UserRepository userRepository;
    @Getter
    @Setter
    private static RecipientRepository recipientRepository;
    @Getter
    @Setter
    private static OperatorRepository operatorRepository;
    @Getter
    @Setter
    private static CategoryGroupRepository categoryGroupRepository;
    @Getter
    @Setter
    private static CategoryRepository categoryRepository;
    @Getter
    @Setter
    private static CountHandlingPlanRepository countHandlingPlanRepository;
    //    @Getter
//    @Setter
//    private static CoursesNameRepository     coursesNameRepository;
//    @Getter
//    @Setter
//    private static CoursesTypeRepository     coursesTypeRepository;
    @Getter
    @Setter
    private static EventRepository eventRepository;
    @Getter
    @Setter
    private static GroupRepository groupRepository;
    //
    @Getter
    @Setter
    private static KpiRepository kpiRepository;
    @Getter
    @Setter
    private static QuestionRepository questionRepository;
    @Getter
    @Setter
    private static QuestMessageRepository questMessageRepository;

    @Getter
    @Setter
    private static ReminderTaskRepository reminderTaskRepository;

    @Getter
    @Setter
    private static ServiceQuestionRepository serviceQuestionRepository;
    @Getter
    @Setter
    private static ServiceSurveyAnswerRepository serviceSurveyAnswerRepository;
    @Getter
    @Setter
    private static SpecialistRepository specialistRepository;
    @Getter
    @Setter
    private static SuggestionRepository suggestionRepository;
    @Getter
    @Setter
    private static ComplaintRepository complaintRepository;
    @Getter
    @Setter
    private static SurveyAnswerRepository surveyAnswerRepository;

    @Getter
    @Setter
    private static ConsultationRepository consultationRepository;
    @Getter
    @Setter
    private static CategoriesIndicatorRepository categoriesIndicatorRepository;

    @Getter
    @Setter
    private static ServiceRepository serviceRepository;

    @Getter
    @Setter
    private static ServicesSpecsRepository servicesSpecsRepository;

    @Getter
    @Setter
    private static RegistrationServiceRepository registrationServiceRepository;

    @Getter
    @Setter
    private static ComesCourseRepository comesCourseRepository;

    @Getter
    @Setter
    private static DirectionRepository directionRepository;

    @Getter
    @Setter
    private static DirectionRegistrationRepository directionRegistrationRepository;

    @Getter
    @Setter
    private static ReportServiceRepository reportServiceRepository;
    @Getter
    @Setter
    private static UpravlenieRepository upravlenieRepository;
    @Getter
    @Setter
    private static RegistrationEventRepository registrationEventRepository;
    @Getter
    @Setter
    private static StatusRepository statusRepository;

    @Autowired
    public TelegramBotRepositoryProvider
            (PropertiesRepository propertiesRepository, LanguageUserRepository languageUserRepository,
             ButtonRepository buttonRepository, MessageRepository messageRepository, KeyboardRepository keyboardRepository,
             UserRepository userRepository, RecipientRepository recipientRepository, OperatorRepository operatorRepository,
             CategoryRepository categoryRepository, CategoryGroupRepository categoryGroupRepository, CountHandlingPlanRepository countHandlingPlanRepository,
             EventRepository eventRepository,
             GroupRepository groupRepository, KpiRepository kpiRepository,
             QuestMessageRepository questMessageRepository, QuestionRepository questionRepository,
             ReminderTaskRepository reminderTaskRepository,
             ServiceSurveyAnswerRepository serviceSurveyAnswerRepository, ServiceQuestionRepository serviceQuestionRepository,
             SpecialistRepository specialistRepository, SuggestionRepository suggestionRepository,
             SurveyAnswerRepository surveyAnswerRepository, AdminRepository adminRepository,
             ComplaintRepository complaintRepository,
             ConsultationRepository consultationRepository,

             StatusRepository statusRepository,
             CategoriesIndicatorRepository categoriesIndicatorRepository, ServiceRepository serviceRepository,
             ServicesSpecsRepository servicesSpecsRepository, RegistrationServiceRepository registrationServiceRepository,
             ComesCourseRepository comesCourseRepository, DirectionRepository directionRepository,
             DirectionRegistrationRepository directionRegistrationRepository,
             ReportServiceRepository reportServiceRepository,
             UpravlenieRepository upravlenieRepository, RegistrationEventRepository registrationEventRepository

            ) {
        setPropertiesRepository(propertiesRepository);
        setLanguageUserRepository(languageUserRepository);
        setButtonRepository(buttonRepository);
        setMessageRepository(messageRepository);
        setKeyboardRepository(keyboardRepository);
        setUserRepository(userRepository);
        setRecipientRepository(recipientRepository);
        setOperatorRepository(operatorRepository);
        setCategoryGroupRepository(categoryGroupRepository);
        setCategoryRepository(categoryRepository);
        setCountHandlingPlanRepository(countHandlingPlanRepository);
        setEventRepository(eventRepository);
        setGroupRepository(groupRepository);
        setKpiRepository(kpiRepository);
        setQuestionRepository(questionRepository);
        setQuestMessageRepository(questMessageRepository);
        setReminderTaskRepository(reminderTaskRepository);
        setServiceQuestionRepository(serviceQuestionRepository);
        setServiceSurveyAnswerRepository(serviceSurveyAnswerRepository);
        setSpecialistRepository(specialistRepository);
        setSuggestionRepository(suggestionRepository);
        setSurveyAnswerRepository(surveyAnswerRepository);
        setAdminRepository(adminRepository);
        setComplaintRepository(complaintRepository);
        setConsultationRepository(consultationRepository);
        setRegistrationEventRepository(registrationEventRepository);
        setStatusRepository(statusRepository);

        setCategoriesIndicatorRepository(categoriesIndicatorRepository);
        setServiceRepository(serviceRepository);
        setServicesSpecsRepository(servicesSpecsRepository);
        setRegistrationServiceRepository(registrationServiceRepository);
        setComesCourseRepository(comesCourseRepository);
        setDirectionRepository(directionRepository);
        setDirectionRegistrationRepository(directionRegistrationRepository);
        setReportServiceRepository(reportServiceRepository);
        setUpravlenieRepository(upravlenieRepository);
    }
}
