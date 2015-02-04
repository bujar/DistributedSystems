/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Keane
 */

public class TimeStamp {

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
