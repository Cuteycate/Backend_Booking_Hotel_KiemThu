# Booking Application

## Description
This Booking Application is a web platform that allows users to book hotels online. Built with **Spring Boot** for the backend and **React + Vite** for the frontend, the application enables users to search for hotels, make reservations, and manage bookings seamlessly.

### Motivation
The project was developed to provide a user-friendly and secure hotel booking system, enhancing the experience of both customers and rental providers.

### Why was this built?
This project was created to simplify the process of hotel booking by offering a modern user interface, robust authentication, and a seamless reservation experience.

### Problem it solves
The system addresses issues such as spam registrations, inefficient booking management, lack of real-time availability checks, and seamless online payment integration.

### What did I learn?
Through this project, I gained experience in:
- Implementing **JWT authentication** for secure API communication.
- Integrating **email confirmation** and **password recovery**.
- Utilizing **React + Vite** for a fast and optimized frontend.
- Managing real-time hotel availability and search suggestions.
- Implementing **online payment** for deposits and full payments.
- Handling **multi-role access control** for users, rentals, and admins.

## Table of Contents
- [Installation](#installation)
- [Usage](#usage)
- [Credits](#credits)
- [Features](#features)

## Installation

### Backend (Spring Boot)
#### Prerequisites
- Java 17+
- Maven
- PostgreSQL or MySQL database

#### Steps
1. Clone the repository from GitHub.
2. Configure the database in `application.properties`.
3. Run the backend using:
   ```sh
   mvn spring-boot:run
   ```

### Frontend (React + Vite)
The project consists of three separate frontends: **Admin, Rental, User**.

#### Prerequisites
- Node.js (v16 or higher)

#### Steps
1. Navigate to each frontend folder (`admin-frontend`, `rental-frontend`, `user-frontend`).
2. Install dependencies:
   ```sh
   npm install
   ```
3. Run the frontend:
   ```sh
   npm run dev
   ```

## Usage
Once both the backend and frontend are running, navigate to `http://localhost:3000` in your browser.

Below are some screenshots showcasing key features:

### Homepage:

#### Home:
![Homepage](https://github.com/user-attachments/assets/ff93ee46-9a0c-49c0-af9f-55e107c51b9e)
#### Login:
![Login](https://github.com/user-attachments/assets/e7fb170a-86f6-4533-9b0a-bd6d55d26d6b)
#### Register:
![Register](https://github.com/user-attachments/assets/032cfa94-9259-4fbe-97ed-e718ab9f7c11)
#### Forgot Password:
![Forgot Password](https://github.com/user-attachments/assets/32b33fc9-7ead-4e72-b7ff-fbe201971009)
#### Reset Password Email
![Reset Password Email1](https://github.com/user-attachments/assets/fc11131e-46ab-4bcf-9075-8450c6a88a46)
![Reset Password Email2](https://github.com/user-attachments/assets/a19f6dff-355e-4c0a-82b4-3b3ab311c94d)




### Hotel Search:
#### Location-Based Search
![Location-Based Search1](https://github.com/user-attachments/assets/bdbf259f-9d47-468f-9559-041173c91284)
![Location-Based Search2](https://github.com/user-attachments/assets/94c2677c-4823-4c65-a716-686c61c67e32)

#### Hotel Name Search
![Hotel Name Search1](https://github.com/user-attachments/assets/2c8fdd12-1351-4ee4-a0b5-81ef3b93df40)
![Hotel Name Search2](https://github.com/user-attachments/assets/135fa184-c080-4a87-b528-0bab8d219edd)






### Booking Process:
#### Real-Time Room Availability Check  
- Each room type has a **maximum booking capacity of 4 guests**.  
- The system will display the **real-time availability** of rooms.  
- If a room type is **fully booked**, it will **no longer be shown** in the search results.

![Real-Time Room Availability Check](https://github.com/user-attachments/assets/5bd96b22-3fd7-41a5-a6a2-e0e95e1f12f0)

### Payment Integration (Online Deposit/Full Payment):
#### Payment Rules  
- If **no deposit is required**, users can either **book immediately** or **pay in full**.


- If a **deposit is required**, users must either **pay the deposit** or **pay the full amount** to complete the booking.
![pay the deposi](https://github.com/user-attachments/assets/46830e26-2f36-4495-9ba5-ec6057ccfed7)



#### Payment
![Payment](https://github.com/user-attachments/assets/ed0a0ada-1249-49d5-954a-737290f36c9f)
![Payment Success](https://github.com/user-attachments/assets/2463b0a5-ea76-4172-8d61-b2e07b777180)




### Email Confirmation:

![Email Confirmation](https://github.com/user-attachments/assets/47e966ae-55d8-423e-809c-e9dab6ff0755)


### Admin Dashboard:

![image](https://github.com/user-attachments/assets/b42136e4-46dd-4206-bf82-297ba73ab005)
![image](https://github.com/user-attachments/assets/1735190e-4494-4506-8735-4e1f3d9860be)
![image](https://github.com/user-attachments/assets/14b193dd-32bf-4e06-8c4b-86f56487b89f)
![image](https://github.com/user-attachments/assets/488616b3-aede-4164-89e7-b44664212a2e)
![image](https://github.com/user-attachments/assets/90bf42f5-754a-47bc-b690-51e5a20bcd3b)
![image](https://github.com/user-attachments/assets/ce994a18-fe8b-44e2-a569-4071bbe2b0c5)
![image](https://github.com/user-attachments/assets/90b18015-c52c-4582-8305-7063ab6e92f2)





### Rental Management:

#### Hotel Registration & Approval  
- Rentals can **register a hotel** and wait for **admin approval**.  
- Once approved, they can **manage and edit** their hotel details.
![image](https://github.com/user-attachments/assets/ba5c00ef-3832-45ea-882f-cf52d70f2050)
![image](https://github.com/user-attachments/assets/e66df97e-ff58-4dc8-af25-07acc045939e)
![image](https://github.com/user-attachments/assets/64a7fb1a-9b39-4c0f-a8ec-6704b780a1cd)
![image](https://github.com/user-attachments/assets/6481e8dd-b84d-4378-ad31-f09215497b89)





#### Room Management  
- Rentals can **add new rooms** to their hotel.  
- They can also **edit and update** room details.
![image](https://github.com/user-attachments/assets/579832db-c774-4201-91ba-fd70971cf7c3)
![image](https://github.com/user-attachments/assets/3d02c320-db76-452e-94ac-f6ef631c5e37)
![image](https://github.com/user-attachments/assets/9ab1135e-eb80-4765-acd3-859bca403b19)




#### Booking Management  
- Rentals can **manage user bookings** and **confirm reservations**.  
- They can also **assist users with check-in** using the **booking code**.
![image](https://github.com/user-attachments/assets/d5ded13d-2591-43cb-8f7f-283276a1f7c8)
![image](https://github.com/user-attachments/assets/cc754f6a-b045-4bd9-83ac-b169f08c1aeb)
![image](https://github.com/user-attachments/assets/5d90c970-2924-4319-bf9f-b88e97dd9081)

## Features
- **JWT Authentication** for secure API requests.
- **User Registration/Login** with email confirmation.
- **Hotel Search & Filtering** based on location and availability.
- **Dynamic Hotel & Room Display** with real-time availability.
- **Booking System** with date selection and room limit.
- **Payment Integration** for deposits and full payments.
- **Admin Panel** for user and rental management.
- **Rental Management** for hotel owners to add/manage properties.
- **Email Notifications** for booking confirmation and check-in codes.
- **Responsive Design** for mobile and desktop users.

