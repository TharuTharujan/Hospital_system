package com.dsa.hospital_system;

import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

public class Doctor {
    String d_ID, name, spec, avai_slots;
    double consult_fee;
    ArrayList<Integer> slotCapacity;   // SAME CAPACITY FOR ALL SLOTS

    // Constructor
    Doctor(String doctorID, String name, String specialization, String availableSlots, double consultationFee, ArrayList<Integer> slotCapacity) {
        this.d_ID = doctorID;
        this.name = name;
        this.spec = specialization;
        this.avai_slots = availableSlots;
        this.consult_fee = consultationFee;
        this.slotCapacity = slotCapacity;
    }

    // Display one doctor
    void display() {
        System.out.println("Doctor ID          : " + d_ID);
        System.out.println("Name               : Dr. " + name);
        System.out.println("Specialization     : " + spec);
        System.out.println("Available Slots    : " + avai_slots);
        System.out.println("Slot Capacity      : " + slotCapacity);
        System.out.println("Consultation Fee   : Rs " + consult_fee);
        System.out.println("--------------------------------------------------");
    }

    // ========================= SAVE TO FILE =========================
    public static void saveToTextFile(ArrayList<Doctor> doctorList) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("Doctors_List.txt"))) {
            writer.println("=============================================================");
            writer.println("         HOSPITAL DOCTOR MANAGEMENT SYSTEM");
            writer.println("                   Doctor Records");
            writer.println("=============================================================");
            writer.println();

            if (doctorList.isEmpty()) {
                writer.println(" No doctors registered yet.");
            } else {
                for (Doctor d : doctorList) {
                    writer.println("Doctor ID : " + d.d_ID);
                    writer.println("Name : Dr. " + d.name);
                    writer.println("Specialization : " + d.spec);
                    writer.println("Available Slots : " + d.avai_slots);
                    writer.println("Slot Capacities : " + d.slotCapacity);
                    writer.println("Consultation Fee : Rs " + d.consult_fee);
                    writer.println("-------------------------------------------------------------");
                }
            }
            writer.println("\nTotal Doctors: " + doctorList.size());
            writer.println("\nGenerated on: " + java.time.LocalDateTime.now());
            System.out.println("\nDoctor records saved successfully to 'Doctors_List.txt'");
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    // ========================= LOAD FROM FILE (FIXED) =========================
    public static void loadFromFile(ArrayList<Doctor> doctorList) {
        File file = new File("Doctors_List.txt");
        if (!file.exists()) {
            System.out.println("No previous data found. Starting fresh...");
            return;
        }

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();

                if (line.startsWith("Doctor ID :")) {
                    String d_ID = line.substring("Doctor ID :".length()).trim();

                    String nameLine = sc.hasNextLine() ? sc.nextLine().trim() : "";
                    String name = nameLine.startsWith("Name : Dr. ") ? nameLine.substring("Name : Dr. ".length()).trim() : "";

                    String specLine = sc.hasNextLine() ? sc.nextLine().trim() : "";
                    String spec = specLine.startsWith("Specialization :") ? specLine.substring("Specialization :".length()).trim() : "";

                    String slotLine = sc.hasNextLine() ? sc.nextLine().trim() : "";
                    String slots = slotLine.startsWith("Available Slots :") ? slotLine.substring("Available Slots :".length()).trim() : "";

                    String capLine = sc.nextLine().trim();
                    ArrayList<Integer> slotCap = new ArrayList<>();
                    if (capLine.startsWith("Slot Capacities :")) {
                        String list = capLine.substring("Slot Capacities :".length()).trim();
                        list = list.replace("[", "").replace("]", "");
                        if (!list.isEmpty()) {
                            for (String num : list.split(",")) {
                                slotCap.add(Integer.parseInt(num.trim()));
                            }
                        }
                    }
                    
                    String feeLine = sc.hasNextLine() ? sc.nextLine().trim() : "";
                    double fee = 0;
                    if (feeLine.startsWith("Consultation Fee : Rs")) {
                        String feeText = feeLine.substring("Consultation Fee : Rs".length()).trim();
                        fee = Double.parseDouble(feeText.replaceAll("[^0-9.]", ""));
                    }

                    // Skip separator
                    if (sc.hasNextLine()) sc.nextLine();

                    Doctor d = new Doctor(d_ID, name, spec, slots, fee, slotCap);
                    doctorList.add(d);
                }
            }
            System.out.println("Successfully loaded " + doctorList.size() + " doctor(s) from file.\n");
        } catch (Exception e) {
            System.out.println("Warning: Could not load file (might be corrupted). Starting empty.");
        }
    }

    // ========================= GET DOCTOR DETAILS (NO STREAM → NO ERROR) =========================
    public static Doctor getDoctorDetails(Scanner input, ArrayList<Doctor> doctorList) {
        String doctorID;
        while (true) {
            System.out.print("Enter Doctor ID: ");
            doctorID = input.nextLine().trim();

            boolean exists = false;
            for (Doctor d : doctorList) {
                if (d.d_ID.equalsIgnoreCase(doctorID)) {
                    exists = true;
                    break;
                }
            }

            if (exists) {
                System.out.println("This ID already exists! Try another one.");
            } else {
                break;
            }
        }

        System.out.print("Enter Doctor Name: ");
        String name = input.nextLine().trim();

        System.out.print("Enter Specialization: ");
        String spec = input.nextLine().trim();

        System.out.print("Enter Available Time Slots (e.g., 9AM-12PM, 2PM-5PM): ");
        String slots = input.nextLine().trim();
        
        String[] splitSlots = slots.split(",");
        
        System.out.print("Enter Appointment Capacity for EACH Slot: ");
        int commonCap = Integer.parseInt(input.nextLine().trim());

        // create capacity list with same value
        ArrayList<Integer> slotCapacity = new ArrayList<>();
        for (int i = 0; i < splitSlots.length; i++) {
            slotCapacity.add(commonCap);
        }

        System.out.print("Enter Consultation Fee: ");
        double fee = Double.parseDouble(input.nextLine().trim());

        return new Doctor(doctorID, name, spec, slots, fee, slotCapacity);
    }

    // ========================= EDIT DOCTOR =========================
    public static void editDoctor(ArrayList<Doctor> doctorList, Scanner input) {
        System.out.print("Enter Doctor ID to Edit: ");
        String id = input.nextLine().trim();

        for (Doctor d : doctorList) {
            if (d.d_ID.equalsIgnoreCase(id)) {
                System.out.println("Editing Dr. " + d.name);
                while (true) {
                    System.out.println("\n1. Name\n2. Specialization\n3. Available Slots\n4. Slot Capacity\n5. Fee\n6. Finish");
                    System.out.print("Choice: ");
                    int ch = Integer.parseInt(input.nextLine());

                    switch (ch) {
                        case 1: System.out.print("New Name: "); d.name = input.nextLine().trim(); break;
                        case 2: System.out.print("New Specialization: "); d.spec = input.nextLine().trim(); break;
                        case 3: System.out.print("New Slots: "); d.avai_slots = input.nextLine().trim(); break;
                        case 4:
                            System.out.print("New Capacity (same for all slots): ");
                            int newCap = Integer.parseInt(input.nextLine().trim());
                            d.slotCapacity.clear();
                            String[] parts = d.avai_slots.split(",");
                            for (int i = 0; i < parts.length; i++)
                                d.slotCapacity.add(newCap);
                            break;
                        case 5: System.out.print("New Fee: "); d.consult_fee = Double.parseDouble(input.nextLine().trim()); break;
                        case 6: saveToTextFile(doctorList); System.out.println("Doctor updated!"); return;
                        default: System.out.println("Invalid choice!");
                    }
                    System.out.println("Updated!");
                }
            }
        }
        System.out.println("Doctor not found!");
    }

    // ========================= DELETE DOCTOR =========================
    public static void deleteDoctor(ArrayList<Doctor> doctorList, Scanner input) {
        System.out.print("Enter Doctor ID to Delete: ");
        String id = input.nextLine().trim();

        for (int i = 0; i < doctorList.size(); i++) {
            if (doctorList.get(i).d_ID.equalsIgnoreCase(id)) {
                System.out.println("Deleted: Dr. " + doctorList.get(i).name);
                doctorList.remove(i);
                saveToTextFile(doctorList);
                return;
            }
        }
        System.out.println("Doctor not found!");
    }

    // ========================= MENU =========================
    public static void menu(ArrayList<Doctor> doctorList) {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== DOCTOR MANAGEMENT MENU ===");
            System.out.println("1. View All Doctors");
            System.out.println("2. Add New Doctor");
            System.out.println("3. Edit Doctor");
            System.out.println("4. Delete Doctor");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            
            int choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    if (doctorList.isEmpty()) System.out.println("No doctors yet.");
                    else for (Doctor d : doctorList) d.display();
                    break;
                case 2:
                    Doctor newDoc = getDoctorDetails(input, doctorList);
                    doctorList.add(newDoc);
                    saveToTextFile(doctorList);
                    System.out.println("Doctor added successfully!");
                    break;
                case 3:
                    editDoctor(doctorList, input);
                    break;
                case 4:
                    deleteDoctor(doctorList, input);
                    break;
                case 5:
                    System.out.println("Goodbye! All data saved.");
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    // ========================= MAIN =========================
    public static void main(String[] args) {
        System.out.println("=== HOSPITAL DOCTOR MANAGEMENT SYSTEM ===\n");

        ArrayList<Doctor> doctorList = new ArrayList<>();
        loadFromFile(doctorList);    // This loads old data
        menu(doctorList);   // Start the menu
    }
}