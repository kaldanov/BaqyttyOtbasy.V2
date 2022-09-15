package com.telegrambot.command;

import com.telegrambot.exception.NotRealizedMethodException;
import com.telegrambot.command.impl.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Slf4j
public class CommandFactory {

    private static Map<Integer, Class<? extends Command>> mapCommand;

    public static Command getCommand(int id) {
//        System.out.println(id);
        return getFormMap(id).orElseThrow(() -> new NotRealizedMethodException("Not realized for type: " + id));
    }

    private static void addCommand(Class<? extends Command> commandClass) {
        int id = -1;
        try {
            id = getId(commandClass.getName());
        } catch (Exception e) {
            log.warn("Command {} no has id, id set {}", commandClass, id);
        }
        if (id > 0 && mapCommand.get(id) != null)
            log.warn("ID={} has duplicate command {} - {}", id, commandClass, mapCommand.get(id));
        mapCommand.put(id, commandClass);
    }

    private static int getId(String commandName) {
        return Integer.parseInt(commandName.split("_")[0].replaceAll("[^0-9]", ""));
    }

    private static Optional<Command> getFormMap(int id) {
//        System.out.println(id);
        if (mapCommand == null) {
            init();
        }
        //System.out.println(mapCommand);
        try {
            return Optional.of(mapCommand.get(id).newInstance());
        } catch (Exception e) {
            log.error("Command caput: ", e);
        }
        return Optional.empty();
    }

    private static void init() {
        mapCommand = new HashMap<>();
        addCommand(id001_ShowInfo.class);
        addCommand(id002_SelectionLanguage.class);
        addCommand(id003_Suggestion.class);
        addCommand(id004_FirstRegistration.class);

        addCommand(id009_ShowEvent.class);
        addCommand(id010_Survey.class);
        addCommand(id011_ShowAdminInfo.class);
        addCommand(id012_EditMenu.class);
        addCommand(id013_AddSurvey.class);
        addCommand(id014_EditSurvey.class);
        addCommand(id015_EditAdmin.class);
        addCommand(id016_ReportSuggestion.class);
        addCommand(id017_ReportSurvey.class);
        addCommand(id018_ReportProfile.class);
        addCommand(id019_EditEvent.class);
        addCommand(id020_ReportService.class);
        addCommand(id021_Registration.class);
//        addCommand(id022_Consultation.class);
        addCommand(id023_Complaint.class);
        addCommand(id024_ReportComplaint.class);
        addCommand(id025_ShowUsers.class);
        addCommand(id026_EditGroup.class);
        addCommand(id027_Edit_Obrash.class);
//        addCommand(id027_AddHandling.class);
        addCommand(id028_Kpi.class);
        addCommand(id029_Reminder.class);
        addCommand(id030_WeekReport.class);
        addCommand(id031_StructureShowInfo.class);
        addCommand(id032_SpecialistInfo.class);
        addCommand(id033_SpecShowInfo.class);
        addCommand(id034_MapLocationSend.class);
        addCommand(id035_SpecialistEdit.class);
        addCommand(id036_Photo.class);
//        addCommand(id037_ReadactHandling.class);
        addCommand(id038_ReportCourses.class);
        addCommand(id039_EditOperator.class);
        addCommand(id040_MassovayaOtmetka.class);
//        addCommand(id041_IsOperator.class);
        addCommand(id042_OperRegistration.class);
        addCommand(id043_Add_Category_Indicator.class);
        addCommand(id044_AddNewSpecialist.class);
        addCommand(id045_RequestsForServices.class);
//        addCommand(id046_Edit_Directions.class);
        addCommand(id047_Upravlenie.class);
        addCommand(id050_RecipientBySpecialist.class);
        addCommand(id051_ProvidedServiceOnline.class);
        addCommand(id052_EditMenuButtons.class);
        addCommand(id053_EditMenuMessages.class);
        addCommand(id055_Sverka.class);
        addCommand(id056_Poisk.class);
        addCommand(id057_ReportService.class);
        printListCommand();
    }

    private static void printListCommand() {
        StringBuilder stringBuilder = new StringBuilder();
        new TreeMap<>(mapCommand).forEach((y, x) -> stringBuilder.append(x.getSimpleName()).append("\n"));
        log.info("List command:\n{}", stringBuilder.toString());
    }
}
