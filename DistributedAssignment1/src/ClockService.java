
import static java.lang.Math.max;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Keane
 */
public abstract class ClockService {

    public abstract TimeStamp getTimestamp();

    public abstract void updateTimeStamp(TimeStamp newstamp);

    public abstract boolean happenedBefore(TimeStamp otherstamp);

    public abstract boolean happenedAfter(TimeStamp otherstamp);

    public abstract boolean concurrentWith(TimeStamp otherstamp);
}

class LogicalClock extends ClockService {

    TimeStamp stamp;

    public LogicalClock() {
        stamp = new TimeStamp("logical", 0);
    }

    @Override
    public TimeStamp getTimestamp() {
        stamp.value[0]++;
        return stamp;
    }

    @Override
    public void updateTimeStamp(TimeStamp newstamp) {
        stamp.value[0] = max(stamp.value[0], newstamp.value[0] + 1);
    }

    @Override
    public boolean happenedBefore(TimeStamp otherstamp) {
        System.out.println("Logical Clock cannot make \"Happened Before\" comparison");
        return false;
    }

    @Override
    public boolean concurrentWith(TimeStamp otherstamp) {
        System.out.println("Logical Clock cannot make \"Concurrent With\" comparison");
        return false;
    }

    @Override
    public boolean happenedAfter(TimeStamp otherstamp) {
        System.out.println("Logical Clock cannot make \"Happened After\" comparison");
        return false;
    }
}

class VectorClock extends ClockService {

    TimeStamp stamp;
    int place;

    public VectorClock(int size, int newplace) {
        stamp = new TimeStamp("vector", size);
        place = newplace;
    }

    @Override
    public TimeStamp getTimestamp() {
        stamp.value[place]++;
        return stamp;
    }

    @Override
    public void updateTimeStamp(TimeStamp newstamp) {
        for (int i = 0; i < stamp.value.length; i++) {
            stamp.value[i] = max(stamp.value[i], newstamp.value[i]);
        }
    }

    @Override
    public boolean happenedBefore(TimeStamp otherstamp) {
        return stamp.happenedBefore(otherstamp);
    }

    @Override
    public boolean concurrentWith(TimeStamp otherstamp) {
        return stamp.concurrentWith(otherstamp);
    }

    @Override
    public boolean happenedAfter(TimeStamp otherstamp) {
        return stamp.happenedAfter(otherstamp);
    }
}

class TimeStamp {

    int[] value;

    public TimeStamp(String type, int vectorlength) {
        if (type.equals("logical")) {
            this.value = new int[1];
            this.value[0] = 0;
        } else if (type.equals("vector")) {
            this.value = new int[vectorlength];
        }
    }

    public boolean happenedBefore(TimeStamp otherstamp) {
        boolean equal = true;
        boolean lessthan = true;
        for (int i = 0; i < this.value.length; i++) {
            if (this.value[i] != otherstamp.value[i]) {
                equal = false;
            }
            if (this.value[i] > otherstamp.value[i]) {
                lessthan = false;
            }
        }
        return !equal && lessthan;
    }

    public boolean concurrentWith(TimeStamp otherstamp) {
        boolean equal = true;
        boolean lessthan = true;
        boolean greaterthan = true;
        for (int i = 0; i < this.value.length; i++) {
            if (this.value[i] != otherstamp.value[i]) {
                equal = false;
            }
            if (this.value[i] > otherstamp.value[i]) {
                lessthan = false;
            }
            if (this.value[i] < otherstamp.value[i]) {
                greaterthan = false;
            }
        }
        return equal || (!lessthan && !greaterthan);
    }

    public boolean happenedAfter(TimeStamp otherstamp) {
        boolean equal = true;
        boolean greaterthan = true;
        for (int i = 0; i < this.value.length; i++) {
            if (this.value[i] != otherstamp.value[i]) {
                equal = false;
            }
            if (this.value[i] < otherstamp.value[i]) {
                greaterthan = false;
            }
        }
        return !equal && greaterthan;
    }
}
