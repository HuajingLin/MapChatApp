package edu.temple.mapchatapp;

import java.util.Comparator;

public class PartnerComparator implements Comparator<User> {
    @Override
    public int compare(User o1, User o2) {
        if(o1.distanceTo(o1.me) > o2.distanceTo(o1.me))
            return 1;
        return 0;
    }
}
