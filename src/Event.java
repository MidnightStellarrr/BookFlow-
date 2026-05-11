import java.time.LocalDate;
import java.time.LocalTime;

public class Event implements Comparable<Event> {
    private int id;
    private String title;
    private LocalDate date;
    private LocalTime time;
    private int duration; // minutes
    
    public Event(int id, String title, LocalDate date, LocalTime time, int duration) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.duration = duration;
    }
    
    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public int getDuration() { return duration; }
    public LocalTime getEndTime() { return time.plusMinutes(duration); }
    
    @Override
    public int compareTo(Event other) {
        // Sort by date first, then time
        int dateCompare = this.date.compareTo(other.date);
        if (dateCompare != 0) return dateCompare;
        return this.time.compareTo(other.time);
    }
    
    @Override
    public String toString() {
        return String.format("%s - %s: %s (%d min)", date, time, title, duration);
    }
}