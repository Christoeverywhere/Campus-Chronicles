# Campus-Chronicles

> **Transforming Campus Feedback into an Engaging Gamified Experience**

Campus Chronicles is a next-generation **gamified feedback platform** designed to revolutionize how educational institutions collect, analyze, and act upon stakeholder feedback. Instead of traditional survey forms that often result in low participation and poor engagement, Campus Chronicles transforms the feedback process into an interactive campus-building game where users earn rewards, unlock achievements, and contribute to institutional improvement.

The platform combines **gamification, cloud computing, role-based authentication, and real-time analytics** to create a feedback ecosystem that benefits students, faculty, administrative staff, and institutional decision-makers.

---

# 📖 Overview

Traditional feedback systems suffer from several limitations:

* Low student participation
* Survey fatigue
* Lack of motivation
* Poor data quality
* Minimal engagement
* Delayed institutional insights

Campus Chronicles addresses these challenges by embedding feedback collection into a rewarding virtual campus experience where every completed survey contributes to both the user's in-game progress and the institution's understanding of campus satisfaction.

---

# ✨ Key Features

## 🎯 Gamified Feedback Collection

Users earn:

* 🪙 Coins
* ⭐ Experience Points (XP)
* 🏆 Achievements
* 🎖️ Badges
* 👼 Avatar Progression

for completing institutional feedback surveys.

---

## 🏫 Virtual Campus

The application contains an interactive campus map where users can:

* Explore buildings
* Unlock new locations
* Upgrade campus structures
* Complete building-specific surveys
* View campus progression

Buildings represent real institutional departments such as:

* Library
* Academic Blocks
* Sports Complex
* Administration
* Hostels
* Canteens
* Laboratories
* Student Service Centers

---

## 📊 Smart Survey Engine

Administrators can create:

* Academic Surveys
* Infrastructure Feedback
* Event Feedback
* Faculty Evaluation
* Canteen Reviews
* Hostel Reviews
* Administrative Surveys
* Sports Facility Feedback

Question types include:

* Likert Scale
* Multiple Choice
* Yes/No
* Short Answer
* Rating Scale

---

# 🔐 Secure Authentication

Campus Chronicles uses **Firebase Authentication with Google Sign-In**.

Only authorized institutional Google Workspace accounts are allowed to access the platform.

Authentication flow:

```
Google Sign-In
        │
        ▼
Firebase Authentication
        │
        ▼
Institution Email Verification
        │
        ▼
Firestore User Profile
        │
        ▼
Student / Faculty / Staff / Admin
```

No passwords are stored within the application.

---

# 👥 Multi-Role Architecture

The system supports multiple user roles.

## 🎓 Student

* Submit feedback
* Earn rewards
* Upgrade buildings
* Track achievements
* View leaderboard

---

## 👨‍🏫 Faculty

* Submit departmental feedback
* Participate in institutional surveys
* View assigned activities

---

## 👩‍💼 Non-Teaching Staff

* Submit service-related feedback
* Participate in department-specific surveys

---

## 👨‍💻 Administrator

Administrators can:

* Manage users
* Create surveys
* Assign surveys
* View analytics
* Export reports
* Monitor participation
* Manage buildings
* Configure events

---

# ☁ Backend Architecture

Campus Chronicles uses **Firebase** as its cloud backend.

Services include:

* Firebase Authentication
* Cloud Firestore
* Firebase Storage (future)
* Cloud Functions (future)
* Firebase Analytics (optional)

---

# 🗂 Firestore Database Design

## users

Stores user profiles and game progress.

Example fields:

* UID
* Name
* Email
* Role
* Department
* Semester
* Coins
* XP
* Avatar Stage
* Level
* Profile Completion
* Last Login

---

## questions

Stores all survey questions created by administrators.

Supports:

* Building assignment
* Category
* Question type
* Active status
* Scheduling

---

## responses

Stores every submitted survey response.

Each response includes:

* User ID
* Question ID
* Building
* Answer
* Timestamp

This design enables institution-wide analytics without embedding responses inside user profiles.

---

## achievements

Tracks badges and milestones unlocked by users.

Examples:

* First Feedback
* Campus Explorer
* Library Champion
* Feedback Hero

---

## buildingProgress

Stores each user's campus development state.

Tracks:

* Building Level
* Unlock Status
* XP
* Completion Percentage

---

# 🎮 Game Mechanics

Users progress by participating in institutional activities.

Gameplay loop:

```
Login

↓

Explore Campus

↓

Enter Building

↓

Answer Survey

↓

Earn Coins

↓

Gain XP

↓

Upgrade Buildings

↓

Unlock New Areas

↓

Complete Achievements

↓

Improve Campus
```

---

# 👼 Avatar Evolution

As users contribute more feedback, their avatar evolves through multiple stages representing their engagement within the campus community.

Example progression:

* Seed
* Student
* Explorer
* Guardian
* Campus Hero
* Legend

---

# 📈 Analytics Dashboard

Administrators receive real-time institutional insights including:

* Survey Completion Rate
* Department-wise Analysis
* Building Satisfaction Scores
* User Participation
* Feedback Trends
* Response Distribution
* Engagement Statistics
* Leaderboards
* Exportable Reports

---

# 🔒 Security

Campus Chronicles follows role-based access control.

Students cannot:

* Create surveys
* Modify questions
* View institutional analytics

Administrators can:

* Manage surveys
* Access analytics
* Configure application settings
* View institutional reports

Sensitive authentication data remains managed by Firebase Authentication.

---

# 🛠 Technology Stack

### Frontend

* Kotlin
* Jetpack Compose
* Material Design 3

### Architecture

* MVVM
* Repository Pattern
* Hilt Dependency Injection

### Backend

* Firebase Authentication
* Cloud Firestore

### Local Storage

* Room Database

### Networking

* Firebase SDK

### Navigation

* Jetpack Navigation Compose

---

# 📱 Application Workflow

```
Launch App
      │
      ▼
Splash Screen
      │
      ▼
Google Sign-In
      │
      ▼
Firebase Authentication
      │
      ▼
Role Verification
      │
      ▼
Profile Setup (First Login Only)
      │
      ▼
Student / Faculty / Staff Dashboard
      │
      ▼
Campus Exploration
      │
      ▼
Survey Participation
      │
      ▼
Rewards & Progression
```

---

# 🎯 Objectives

* Increase institutional feedback participation.
* Improve feedback quality through gamification.
* Provide actionable analytics to administrators.
* Encourage continuous campus engagement.
* Replace passive survey systems with interactive experiences.

---

# 🚀 Future Enhancements

* AI-powered sentiment analysis of textual feedback.
* Personalized survey recommendations.
* Blockchain-based audit trail for response integrity.
* Cross-platform support.
* Push notifications for new surveys.
* Achievement marketplace.
* Seasonal campus events.
* Real-time leaderboards.
* Offline survey synchronization.
* AI-powered analytics dashboard.

---

# 📚 Research Significance

Campus Chronicles demonstrates how **gamification principles**, **cloud-based architectures**, and **interactive user experiences** can significantly improve participation rates in institutional feedback systems while maintaining data integrity and providing meaningful insights for academic decision-making.

The platform serves as both an educational technology solution and a research prototype exploring the impact of gamified engagement on feedback quality and participation within higher education institutions.

---

# 📄 License

This project is developed as an academic research and educational application. Licensing and distribution are applied.
