package constructors;

public class Booking {
    String bookingID;
    String residentName;
    int roomNumber;
    String visitorName;
    String date_of_visit;
    String status;

    public Booking(String bookingID, String residentName, int roomNumber, String visitorName, String date_of_visit, String status) {
        this.bookingID = bookingID;
        this.residentName = residentName;
        this.roomNumber = roomNumber;
        this.visitorName = visitorName;
        this.date_of_visit = date_of_visit;
        this.status = status;
    }
    

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getResidentName() {
        return residentName;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getDate_of_visit() {
        return date_of_visit;
    }

    public void setDate_of_visit(String date_of_visit) {
        this.date_of_visit = date_of_visit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
