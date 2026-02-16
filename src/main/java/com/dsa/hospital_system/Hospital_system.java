package com.dsa.hospital_system;

import java.util.ArrayList;
import java.util.Scanner;

public class Hospital_system {

    public static void main(String[] args) {
        ArrayList<Doctor> doctors = new ArrayList<>();
        ArrayList<Patient> patients = new ArrayList<>();

        // Load data on startup
        Doctor.loadFromFile(doctors);
        Patient.loadFromFile(patients);

        BookingSystem manager = new BookingSystem(doctors, patients);

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== HOSPITAL MAIN MENU ===");
            System.out.println("1. Doctor Management (Admin)");
            System.out.println("2. Patient Management (Admin)");
            System.out.println("3. Appointment Management (Booking)");
            System.out.println("4. Search for Doctors"); // <--- NEW FEATURE
            System.out.println("5. Exit");
            System.out.print("Choice: ");
            
            int ch;
            try { ch = Integer.parseInt(sc.nextLine().trim()); } 
            catch (Exception e) { System.out.println("Invalid input."); continue; }

            switch (ch) {
                case 1:
                    Doctor.menu(doctors);
                    // Re-create queues for newly added doctors to ensure system consistency
                    for (Doctor d : doctors) {
                        manager.confirmedMap.putIfAbsent(d.d_ID, new BookingSystem.ConfirmedQueue(50));
                    }
                    break;
                case 2:
                    Patient.menu(patients);
                    break;
                case 3:
                    runBookingMenu(manager, doctors, sc);
                    break;
                case 4:
                    // <--- CALLING NEW SEARCH FUNCTION
                    searchDoctors(doctors, sc);
                    break;
                case 5:
                    manager.saveBookings();
                    Doctor.saveToTextFile(doctors);
                    Patient.saveToFile(patients);
                    System.out.println("Goodbye.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // =============================================================
    // NEW FEATURE: SEARCH DOCTORS
    // =============================================================
    public static void searchDoctors(ArrayList<Doctor> doctorList, Scanner sc) {
        System.out.println("\n=== SEARCH DOCTORS ===");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by Specialization");
        System.out.println("3. Back");
        System.out.print("Enter Choice: ");
        
        int choice;
        try { choice = Integer.parseInt(sc.nextLine().trim()); } 
        catch (Exception e) { System.out.println("Invalid input."); return; }

        if (choice == 3) return;

        String keyword = "";
        if (choice == 1) {
            System.out.print("Enter Doctor Name (or part of it): ");
            keyword = sc.nextLine().trim().toLowerCase();
        } else if (choice == 2) {
            System.out.print("Enter Specialization (e.g., Cardio, ENT): ");
            keyword = sc.nextLine().trim().toLowerCase();
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        boolean found = false;
        System.out.println("\n--- Search Results ---");
        for (Doctor d : doctorList) {
            boolean match = false;
            
            // Search logic: Case insensitive & Partial match
            if (choice == 1 && d.name.toLowerCase().contains(keyword)) {
                match = true;
            } else if (choice == 2 && d.spec.toLowerCase().contains(keyword)) {
                match = true;
            }

            if (match) {
                d.display(); // Uses the existing display method in Doctor.java
                found = true;
            }
        }

        if (!found) {
            System.out.println("No doctors found matching \"" + keyword + "\".");
        }
    }
    // =============================================================

    public static void runBookingMenu(BookingSystem manager, ArrayList<Doctor> doctorList, Scanner sc) {
        while (true) {
            System.out.println("\n=== APPOINTMENT MANAGEMENT ===");
            System.out.println("1. Book Appointment");
            System.out.println("2. Cancel Booking");
            System.out.println("3. Reschedule Booking");
            System.out.println("4. View Confirmed Appointments");
            System.out.println("5. View Waiting List");
            System.out.println("6. Find Booking by ID");
            System.out.println("7. Back to Main Menu");
            System.out.print("Choice: ");
            
            int ch;
            try { ch = Integer.parseInt(sc.nextLine().trim()); } 
            catch (Exception e) { System.out.println("Invalid."); continue; }

            switch (ch) {
                case 1:
                    System.out.print("Enter Patient Name: ");
                    String pname = sc.nextLine().trim();
                    Patient p = manager.findPatientByName(pname);
                    String pmobile = (p != null) ? p.m_no : "";
                    if (p == null) {
                        System.out.print("Patient not registered. Enter Mobile: ");
                        pmobile = sc.nextLine().trim();
                    }

                    System.out.println("\nAvailable Doctors:");
                    for (Doctor d : doctorList) {
                        System.out.println(d.d_ID + " - Dr. " + d.name + " (" + d.spec + ")");
                    }
                    System.out.print("Enter Doctor ID: ");
                    String did = sc.nextLine().trim();
                    Doctor chosen = manager.getDoctorByID(did);
                    if (chosen == null) { System.out.println("Invalid doctor."); break; }

                    String[] slots = chosen.avai_slots.split(",");
                    System.out.println("Available slots:");
                    for (int i = 0; i < slots.length; i++) {
                        System.out.println((i+1) + ". " + slots[i].trim());
                    }
                    System.out.print("Choose slot number: ");
                    int sidx = -1;
                    try { sidx = Integer.parseInt(sc.nextLine().trim()) - 1; }
                    catch(Exception e) { System.out.println("Invalid input."); break; }

                    if (sidx < 0 || sidx >= slots.length) { System.out.println("Invalid slot."); break; }
                    String stime = slots[sidx].trim();

                    System.out.print("Enter date (YYYY-MM-DD): ");
                    String date = sc.nextLine().trim();

                    String bid = manager.bookAppointment(pname, pmobile, did, date, stime);
                    if (bid != null) System.out.println("Success! Booking ID: " + bid);
                    else System.out.println("Booking failed.");
                    break;

                case 2:
                    System.out.print("Enter Booking ID to cancel: ");
                    String cancelId = sc.nextLine().trim();
                    if (manager.cancelBooking(cancelId)) System.out.println("Cancelled successfully.");
                    else System.out.println("Not found.");
                    break;

                case 3:
                    System.out.print("Enter Booking ID to reschedule: ");
                    String oldId = sc.nextLine().trim();

                    System.out.println("\nChoose new doctor:");
                    for (Doctor d : doctorList) System.out.println(d.d_ID + " - Dr. " + d.name);
                    System.out.print("New Doctor ID: ");
                    String newDid = sc.nextLine().trim();

                    System.out.print("New Date (YYYY-MM-DD): ");
                    String newDate = sc.nextLine().trim();

                    Doctor newDoc = manager.getDoctorByID(newDid);
                    if (newDoc == null) { System.out.println("Invalid doctor."); break; }
                    String[] newSlots = newDoc.avai_slots.split(",");
                    for (int i = 0; i < newSlots.length; i++) {
                        System.out.println((i+1) + ". " + newSlots[i].trim());
                    }
                    System.out.print("Choose new slot number: ");
                    int newSlotIdx = Integer.parseInt(sc.nextLine().trim()) - 1;
                    if (newSlotIdx < 0 || newSlotIdx >= newSlots.length) {
                        System.out.println("Invalid slot."); break;
                    }
                    String newTime = newSlots[newSlotIdx].trim();

                    if (manager.rescheduleBooking(oldId, newDid, newDate, newTime)) {
                        System.out.println("Rescheduled successfully.");
                    } else {
                        System.out.println("Reschedule failed.");
                    }
                    break;

                case 4:
                    manager.displayAllConfirmed();
                    break;
                case 5:
                    manager.displayWaiting();
                    break;
                case 6:
                    System.out.print("Enter Booking ID: ");
                    String searchId = sc.nextLine().trim();
                    BookingSystem.AppointmentLocation loc = manager.findBooking(searchId);
                    if (loc != null) {
                        System.out.println("Found in: " + loc.where);
                        loc.appt.display();
                    } else {
                        System.out.println("Not found.");
                    }
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}