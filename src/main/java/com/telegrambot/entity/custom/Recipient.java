package com.telegrambot.entity.custom;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "recipients", schema = "public")
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long     id;

//    private long    chatId;

    private String  fullName;

    @Column(length = 12)
    private String  iin;

    private String  phoneNumber;




    private String  address;
    private String  visa;             // прописка
    private String  apartment;        //наличие жилия
    private String  children;         // иин детей
    //    private String  parentCount;
    private String  socialBenefits;   // Cоциальные льготы

    //private String  maritalStatus;    //семейное положение

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Status> status;           // статус

    private String  aliments;         // алименты
    private String  employmentType;   //занятность
    private String  employment;       //место работы
    //    private String  needAJob;
//    private String  jobType;
    private String  education;        // образование
    private String  educationName;    //Специальность по образованию
    private String  disabilityType;   // тип инвалидности
    private String  disability;       // инвалидность

    //    private String  professionalCourses;
//    private String  educationAndOtherCourses;
//    private String  businessTraining;
//    private String  educationCoursesForKids;
//    private String  artAndMusicCourses;
//    private String  sportSection;
//    private String  socialNeeds;
//    private String  psychoNeed;
//    private String  lawyerNeed;
//    private String  healerForFamily;
    private String  creditHistory;  //Наличие кредита
    private String  creditInfo;     //Информация о кредите

    private Date registrationDate;

    private String district;

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}

