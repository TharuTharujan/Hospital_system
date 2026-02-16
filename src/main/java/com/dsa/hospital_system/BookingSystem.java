package com.dsa.hospital_system;

import java.io.*;
import java.util.*;

public class BookingSystem {

    // ---------------- Appointment model ----------------
    static class Appointment {
        String bookingID, patientName, patientMobile, doctorID, date, time, status;

        Appointment(String bookingID, String patientName, String patientMobile,
                    String doctorID, String date, String time, String status) {
            this.bookingID = bookingID;
            this.patientName = patientName;
            this.patientMobile = patientMobile;
            this.doctorID = doctorID;
            this.date = date;
            this.time = time;
            this.status = status;
        }

        Appointment(String patientName, String patientMobile, String doctorID, String date, String time, String status) {
            this(null, patientName, patientMobile, doctorID, date, time, status);
        }
        
        void display() {
             System.out.println(bookingID + " | " + patientName + " | " + doctorID + " | " + date + " " + time + " | " + status);
        }
    }

    // ---------------- Confirmed Queue (Circular Array) ----------------
    static class ConfirmedQueue {
        private Appointment[] arr;
        private int front = 0, rear = -1, size = 0;
        private final int capacity;

        ConfirmedQueue(int capacity) {
            this.capacity = capacity;
            this.arr = new Appointment[capacity];
        }

        boolean isFull() { return size == capacity; }
        boolean isEmpty() { return size == 0; }

        boolean enqueue(Appointment appt) {
            if (isFull()) return false;
            rear = (rear + 1) % capacity;
            arr[rear] = appt;
            size++;
            return true;
        }

        Appointment removeByBookingID(String bookingID) {
            if (isEmpty()) return null;
            int idx = front;
            int found = -1;
            for (int i = 0; i < size; i++) {
                if (arr[idx] != null && bookingID.equalsIgnoreCase(arr[idx].bookingID)) {
                    found = idx; break;
                }
                idx = (idx + 1) % capacity;
            }
            if (found == -1) return null;

            Appointment removed = arr[found];
            // Shift elements
            for (int i = found; i != rear; i = (i + 1) % capacity) {
                arr[i] = arr[(i + 1) % capacity];
            }
            arr[rear] = null;
            rear = (rear - 1 + capacity) % capacity;
            size--;
            return removed;
        }

        Appointment findByBookingID(String bookingID) {
            if (isEmpty()) return null;
            int idx = front;
            for (int i = 0; i < size; i++) {
                Appointment a = arr[idx];
                if (a != null && bookingID.equalsIgnoreCase(a.bookingID)) return a;
                idx = (idx + 1) % capacity;
            }
            return null;
        }

        int countBookingsForSlot(String date, String time) {
            if (isEmpty()) return 0;
            int count = 0;
            int idx = front;
            for (int i = 0; i < size; i++) {
                Appointment a = arr[idx];
                if (a != null && a.date.equals(date) && a.time.equalsIgnoreCase(time) &&
                    "confirmed".equalsIgnoreCase(a.status)) {
                    count++;
                }
                idx = (idx + 1) % capacity;
            }
            return count;
        }

        List<Appointment> toList() {
            List<Appointment> list = new ArrayList<>();
            if (isEmpty()) return list;
            int idx = front;
            for (int i = 0; i < size; i++) {
                list.add(arr[idx]);
                idx = (idx + 1) % capacity;
            }
            return list;
        }
        
        void displayAll() {
            if (isEmpty()) { System.out.println("No confirmed appointments."); return; }
            int idx = front;
            for (int i = 0; i < size; i++) {
                 arr[idx].display();
                 idx = (idx + 1) % capacity;
            }
        }
    }

    // ---------------- Waiting Queue (Linked List) ----------------
    static class Node { Appointment appt; Node next; Node(Appointment a) { this.appt = a; } }

    static class WaitingQueue {
        Node front = null, rear = null;
        
        boolean isEmpty() { return front == null; }
        
        void enqueue(Appointment a) {
            Node n = new Node(a);
            if (rear == null) front = rear = n;
            else { rear.next = n; rear = n; }
        }
        
        Appointment removeByBookingID(String bookingID) {
            Node prev = null, cur = front;
            while (cur != null) {
                if (cur.appt.bookingID.equalsIgnoreCase(bookingID)) {
                    if (prev == null) front = cur.next;
                    else prev.next = cur.next;
                    if (cur.next == null) rear = prev;
                    return cur.appt;
                }
                prev = cur; cur = cur.next;
            }
            return null;
        }
        
        Appointment findByBookingID(String bookingID) {
            Node cur = front;
            while (cur != null) {
                if (cur.appt.bookingID.equalsIgnoreCase(bookingID)) return cur.appt;
                cur = cur.next;
            }
            return null;
        }
        
        void removeNode(Node target, Node prev) {
            if (prev == null) front = target.next;
            else prev.next = target.next;
            if (target.next == null) rear = prev;
        }

        List<Appointment> toList() {
            List<Appointment> L = new ArrayList<>();
            Node cur = front; while (cur != null) { L.add(cur.appt); cur = cur.next; }
            return L;
        }
        
        void displayAll() {
            if(isEmpty()) System.out.println("Waiting list empty.");
            Node cur = front; while(cur!=null) { cur.appt.display(); cur=cur.next; }
        }
    }

    // ---------------- Booking Manager ----------------
    public Map<String, ConfirmedQueue> confirmedMap = new HashMap<>();
    WaitingQueue waiting = new WaitingQueue();
    
    ArrayList<Doctor> doctorList;
    ArrayList<Patient> patientList;
    private int bookingCounter = 0;
    private static final String BOOKINGS_FILE = "bookings.txt";

    public BookingSystem(ArrayList<Doctor> doctorList, ArrayList<Patient> patientList) {
        this.doctorList = doctorList;
        this.patientList = patientList;
        
        for (Doctor d : doctorList) {
            int totalDailyCapacity = 0;
            if (d.slotCapacity != null) {
                for (int cap : d.slotCapacity) totalDailyCapacity += cap;
            }
            int queueSize = (totalDailyCapacity > 0) ? totalDailyCapacity : 50;
            confirmedMap.put(d.d_ID, new ConfirmedQueue(queueSize));
        }
        loadBookings();
    }

    private String nextBookingID() {
        bookingCounter++;
        return String.format("B%03d", bookingCounter);
    }

    private int getSlotSpecificCapacity(Doctor doc, String timeSlot) {
        if (doc.avai_slots == null || doc.slotCapacity == null) return 0;
        String[] slots = doc.avai_slots.split(",");
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].trim().equalsIgnoreCase(timeSlot.trim())) {
                return (i < doc.slotCapacity.size()) ? doc.slotCapacity.get(i) : 0;
            }
        }
        return 0;
    }

    public Doctor getDoctorByID(String docID) {
        for (Doctor d : doctorList) if (d.d_ID.equalsIgnoreCase(docID)) return d;
        return null;
    }

    // Helper used by menu
    public Patient findPatientByName(String name) {
        for (Patient p : patientList) {
            if (p.name.equalsIgnoreCase(name)) return p;
        }
        return null;
    }

    // ========================= 1. BOOK APPOINTMENT =========================
    public String bookAppointment(String patientName, String patientMobile, String doctorID, String date, String time) {
        Doctor doc = getDoctorByID(doctorID);
        if (doc == null) return null;

        ConfirmedQueue cq = confirmedMap.get(doctorID);
        if (cq == null) return null;

        // Check duplicate in confirmed only
        for (Appointment a : cq.toList()) {
            if (a.patientName.equalsIgnoreCase(patientName) && a.date.equals(date) && a.time.equalsIgnoreCase(time)) {
                System.out.println("Duplicate booking!");
                return null;
            }
        }

        int limitForThisSlot = getSlotSpecificCapacity(doc, time);
        if (limitForThisSlot == 0) {
            System.out.println("Invalid Time Slot.");
            return null;
        }

        int currentCountInSlot = cq.countBookingsForSlot(date, time);

        Appointment a = new Appointment(patientName, patientMobile, doctorID, date, time, "waiting");
        
        if (currentCountInSlot < limitForThisSlot) {
            a.status = "confirmed";
            a.bookingID = nextBookingID();
            cq.enqueue(a);
            saveBookings();
            return a.bookingID;
        } else {
            System.out.println("Slot Full (" + currentCountInSlot + "/" + limitForThisSlot + "). Added to Waiting List.");
            a.status = "waiting";
            a.bookingID = nextBookingID();
            waiting.enqueue(a);
            saveBookings();
            return a.bookingID;
        }
    }

    // ========================= 2. CANCEL APPOINTMENT =========================
    public boolean cancelBooking(String bookingID) {
        for (Map.Entry<String, ConfirmedQueue> e : confirmedMap.entrySet()) {
            Appointment removed = e.getValue().removeByBookingID(bookingID);
            if (removed != null) {
                System.out.println("Booking " + bookingID + " cancelled.");
                saveBookings();
                fillVacancy(e.getKey(), removed.date, removed.time);
                return true;
            }
        }
        Appointment w = waiting.removeByBookingID(bookingID);
        if (w != null) { 
            System.out.println("Waiting booking " + bookingID + " cancelled.");
            saveBookings(); 
            return true; 
        }
        return false;
    }

    // ========================= 3. RESCHEDULE APPOINTMENT =========================
    public boolean rescheduleBooking(String bookingID, String newDoctorID, String newDate, String newTime) {
        Appointment appt = null;
        String oldDoctorID = null, oldDate = null, oldTime = null;

        for (Map.Entry<String, ConfirmedQueue> e : confirmedMap.entrySet()) {
            Appointment found = e.getValue().removeByBookingID(bookingID);
            if (found != null) {
                appt = found;
                oldDoctorID = e.getKey();
                oldDate = found.date;
                oldTime = found.time;
                break;
            }
        }

        if (appt == null) {
            appt = waiting.removeByBookingID(bookingID);
        }

        if (appt == null) {
            System.out.println("Booking ID not found.");
            return false;
        }

        if (oldDoctorID != null) {
            fillVacancy(oldDoctorID, oldDate, oldTime);
        }

        Doctor newDoc = getDoctorByID(newDoctorID);
        ConfirmedQueue newQueue = confirmedMap.get(newDoctorID);
        
        if (newDoc == null || newQueue == null) {
            appt.status = "waiting";
            waiting.enqueue(appt);
            saveBookings();
            return false;
        }

        int limit = getSlotSpecificCapacity(newDoc, newTime);
        int current = newQueue.countBookingsForSlot(newDate, newTime);

        appt.doctorID = newDoctorID;
        appt.date = newDate;
        appt.time = newTime;

        if (current < limit) {
            appt.status = "confirmed";
            newQueue.enqueue(appt);
            System.out.println("Reschedule Successful! Status: Confirmed.");
        } else {
            appt.status = "waiting";
            waiting.enqueue(appt);
            System.out.println("Target slot full. Moved to Waiting List.");
        }
        saveBookings();
        return true;
    }

    // ========================= 4. FILL VACANCY =========================
    private void fillVacancy(String doctorID, String date, String time) {
        ConfirmedQueue cq = confirmedMap.get(doctorID);
        Doctor doc = getDoctorByID(doctorID);
        if (cq == null || doc == null) return;

        int limit = getSlotSpecificCapacity(doc, time);
        int current = cq.countBookingsForSlot(date, time);
        if (current >= limit) return;

        Node prev = null;
        Node cur = waiting.front;

        while (cur != null) {
            Appointment cand = cur.appt;
            if (cand.doctorID.equalsIgnoreCase(doctorID) && 
                cand.date.equals(date) && 
                cand.time.equalsIgnoreCase(time)) {
                
                waiting.removeNode(cur, prev);
                cand.status = "confirmed";
                cq.enqueue(cand);
                System.out.println("Vacancy filled! Booking " + cand.bookingID + " moved to Confirmed.");
                saveBookings();
                return;
            }
            prev = cur;
            cur = cur.next;
        }
    }

    // ========================= VIEW & FILE I/O =========================
    public void displayAllConfirmed() {
        System.out.println("\n=== CONFIRMED APPOINTMENTS ===");
        for (Map.Entry<String, ConfirmedQueue> e : confirmedMap.entrySet()) {
            Doctor doc = getDoctorByID(e.getKey());
            String docName = (doc != null) ? "Dr. " + doc.name : e.getKey();
            System.out.println("\nDoctor: " + docName + " (" + e.getKey() + ")");
            e.getValue().displayAll();
        }
    }

    public void displayWaiting() {
        System.out.println("\n=== WAITING LIST (FIFO) ===");
        waiting.displayAll();
    }

    public static class AppointmentLocation { 
        Appointment appt; String where; 
        AppointmentLocation(Appointment a, String w){this.appt=a;this.where=w;} 
    }

    public AppointmentLocation findBooking(String bookingID) {
        for (ConfirmedQueue cq : confirmedMap.values()) {
            Appointment a = cq.findByBookingID(bookingID);
            if (a != null) return new AppointmentLocation(a, "confirmed");
        }
        Appointment a = waiting.findByBookingID(bookingID);
        if (a != null) return new AppointmentLocation(a, "waiting");
        return null;
    }

    void saveBookings() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            pw.println("BookingCounter:" + bookingCounter);
            pw.println("#CONFIRMED");
            for (ConfirmedQueue cq : confirmedMap.values())
                for (Appointment a : cq.toList()) writeAppt(pw, a);
            pw.println("#WAITING");
            for (Appointment a : waiting.toList()) writeAppt(pw, a);
        } catch (IOException e) { 
            System.out.println("Error saving bookings: " + e.getMessage()); 
        }
    }
    
    private void writeAppt(PrintWriter pw, Appointment a) {
        pw.println("BookingID:" + a.bookingID);
        pw.println("PatientName:" + a.patientName);
        pw.println("PatientMobile:" + a.patientMobile);
        pw.println("DoctorID:" + a.doctorID);
        pw.println("Date:" + a.date);
        pw.println("Time:" + a.time);
        pw.println("Status:" + a.status);
        pw.println("---");
    }
    
    private void loadBookings() {
        File f = new File(BOOKINGS_FILE);
        if (!f.exists()) return;
        try (Scanner sc = new Scanner(f)) {
            String section = "";
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                if (line.startsWith("BookingCounter:")) { 
                    bookingCounter = Integer.parseInt(line.split(":")[1].trim()); 
                    continue; 
                }
                if (line.startsWith("#")) { section = line.substring(1); continue; }
                if (line.startsWith("BookingID:")) {
                    String bid = line.split(":")[1].trim();
                    String pname = sc.nextLine().split(":")[1].trim();
                    String pmob = sc.nextLine().split(":")[1].trim();
                    String did = sc.nextLine().split(":")[1].trim();
                    String date = sc.nextLine().split(":")[1].trim();
                    String time = sc.nextLine().split(":")[1].trim();
                    String status = sc.nextLine().split(":")[1].trim();
                    sc.nextLine(); // ---
                    Appointment a = new Appointment(bid, pname, pmob, did, date, time, status);
                    if ("CONFIRMED".equals(section)) {
                        confirmedMap.computeIfAbsent(did, k -> new ConfirmedQueue(50)).enqueue(a);
                    } else if ("WAITING".equals(section)) {
                        waiting.enqueue(a);
                    }
                }
            }
        } catch (Exception e) { 
            System.out.println("Load bookings error: " + e); 
        }
    }
}