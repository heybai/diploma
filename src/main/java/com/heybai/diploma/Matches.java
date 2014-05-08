package com.heybai.diploma;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heybai on 5/7/14.
 */
public class Matches {

    private List<Match> matches = new ArrayList<Match>();

    public Matches(List<Match> matches) {
        this.matches = matches;
    }

    public int nMatches() {
        return matches.size();
    }

    public List<Match> getMatches() {
        return matches;
    }
}
