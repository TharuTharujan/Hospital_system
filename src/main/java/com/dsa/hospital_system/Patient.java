package com.dsa.hospital_system;

import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

public class Patient {
    String name, city, medic_his, m_no, email;
    int age;

    Patient(String na, String no, String email, String city, String med, int age) {
        this.name = na;
        this.age = age;
        this.m_no = no;
        this.medic_his = med;
        this.email = email;
        this.city = city;
    }

    void display() {
        System.out.println("Name: " + name);
        System.out.println("Mobile Number: " + m_no);
        System.out.println("Age: " + age);
        System.out.println("Medical History: " + medic_his);
        System.out.println("Email: " + email);
        System.out.println("City: " + city);
        System.out.println("----------------------------");
    }

    // Save all patients to text file
    public static void saveToFile(ArrayList<Patient> patientList) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("patients.txt"))) {
            for (Patient p : patientList) {
                writer.println(p.name);
                writer.println(p.m_no);
                writer.println(p.email);
                writer.println(p.city);
                writer.println(p.age);
                writer.println(p.medic_his);
                writer.println("---"); // separator between patients
            }
            System.out.println("Patients saved to patients.txt successfully.");
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    // Load patients from text file at startup
    public static void loadFromFile(ArrayList<Patient> patientList) {
        File file = new File("patients.txt");
        if (!file.exists()) return;

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String name = fileScanner.nextLine();
                if (!fileScanner.hasNextLine()) break;
                String m_no = fileScanner.nextLine();
                if (!fileScanner.hasNextLine()) break;
                String email = fileScanner.nextLine();
                if (!fileScanner.hasNextLine()) break;
                String city = fileScanner.nextLine();
                if (!fileScanner.hasNextLine()) break;
                int age = Integer.parseInt(fileScanner.nextLine());
                if (!fileScanner.hasNextLine()) break;
                String medic_his = fileScanner.nextLine();
                
                // Skip separator line
                if (fileScanner.hasNextLine()) {
                    String separator = fileScanner.nextLine();
                    if (!separator.equals("---")) {
                        // In case format is broken, try to continue
                    }
                }

                Patient p = new Patient(name, m_no, email, city, medic_his, age);
                patientList.add(p);
            }
            System.out.println("Loaded " + patientList.size() + " patient(s) from patients.txt");
        } catch (Exception e) {
            System.out.println("Error loading from file (file may be corrupted): " + e.getMessage());
        }
    }

    public static Patient getPatientDetails(Scanner input) {
        System.out.print("Enter Patient Name: ");
        String name = input.nextLine();
        System.out.print("Enter Mobile Number: ");
        String mobile = input.nextLine();
        System.out.print("Enter Email ID: ");
        String email = input.nextLine();
        System.out.print("Enter City: ");
        String city = input.nextLine();
        System.out.print("Enter Age: ");
        int age = Integer.parseInt(input.nextLine());
        System.out.print("Enter Medical History: ");
        String medicalHistory = input.nextLine();
        return new Patient(name, mobile, email, city, medicalHistory, age);
    }

    public static ArrayList<Patient> addPatients() {
        Scanner input = new Scanner(System.in);
        ArrayList<Patient> patientList = new ArrayList<>();

        // Load existing patients first
        loadFromFile(patientList);

        while (true) {
            Patient p = getPatientDetails(input);
            patientList.add(p);
            saveToFile(patientList); // Auto-save after adding

            System.out.print("\nDo you want to add another patient? (yes/no): ");
            String choice = input.nextLine();
            if (!choice.equalsIgnoreCase("yes")) break;
            System.out.println();
        }
        return patientList;
    }

    public static void editPatient(ArrayList<Patient> patientList, Scanner input) {
        System.out.print("Enter Name of Patient to Edit: ");
        String searchName = input.nextLine();
        boolean found = false;
        for (Patient p : patientList) {
            if (p.name.equalsIgnoreCase(searchName)) {
                found = true;
                System.out.println("\nEditing details for " + p.name);
                while (true) {
                    System.out.println("\nSelect the field you want to edit:");
                    System.out.println("1. Name");
                    System.out.println("2. Mobile Number");
                    System.out.println("3. Email");
                    System.out.println("4. City");
                    System.out.println("5. Age");
                    System.out.println("6. Medical History");
                    System.out.println("7. Finish Editing");
                    System.out.print("Enter your choice: ");
                    int choice = Integer.parseInt(input.nextLine());

                    switch (choice) {
                        case 1:
                            System.out.print("Enter New Name: ");
                            p.name = input.nextLine();
                            break;
                        case 2:
                            System.out.print("Enter New Mobile Number: ");
                            p.m_no = input.nextLine();
                            break;
                        case 3:
                            System.out.print("Enter New Email: ");
                            p.email = input.nextLine();
                            break;
                        case 4:
                            System.out.print("Enter New City: ");
                            p.city = input.nextLine();
                            break;
                        case 5:
                            System.out.print("Enter New Age: ");
                            p.age = Integer.parseInt(input.nextLine());
                            break;
                        case 6:
                            System.out.print("Enter New Medical History: ");
                            p.medic_his = input.nextLine();
                            break;
                        case 7:
                            System.out.println("Editing completed!");
                            saveToFile(patientList); // Save after edit
                            return;
                        default:
                            System.out.println("Invalid choice! Try again.");
                            continue;
                    }
                    System.out.println("Field updated successfully!");
                }
            }
        }
        if (!found) System.out.println("Patient not found!");
    }

    public static void deletePatient(ArrayList<Patient> patientList, Scanner input) {
        System.out.print("Enter Name of Patient to Delete: ");
        String deleteName = input.nextLine();
        boolean removed = false;
        for (int i = 0; i < patientList.size(); i++) {
            if (patientList.get(i).name.equalsIgnoreCase(deleteName)) {
                patientList.remove(i);
                removed = true;
                System.out.println("Patient record deleted successfully!");
                saveToFile(patientList); // Save after delete
                break;
            }
        }
        if (!removed) System.out.println("Patient not found!");
    }

    public static void menu(ArrayList<Patient> patientList) {
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("\n--- PATIENT MANAGEMENT MENU ---");
            System.out.println("1. View All Patients");
            System.out.println("2. Add New Patient");
            System.out.println("3. Edit Patient");
            System.out.println("4. Delete Patient");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    if (patientList.isEmpty()) {
                        System.out.println("No patients recorded yet.");
                    } else {
                        for (Patient p : patientList) p.display();
                    }
                    break;
                case 2:
                    Patient p = getPatientDetails(input);
                    patientList.add(p);
                    saveToFile(patientList);
                    System.out.println("Patient added and saved!");
                    break;
                case 3:
                    editPatient(patientList, input);
                    break;
                case 4:
                    deletePatient(patientList, input);
                    break;
                case 5:
                    saveToFile(patientList);
                    System.out.println("All changes saved. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }

    public static void main(String[] args) {
        ArrayList<Patient> patientList = new ArrayList<>();
        loadFromFile(patientList); // Load previous data
        menu(patientList);
    }
}