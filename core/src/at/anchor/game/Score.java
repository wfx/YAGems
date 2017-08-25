package at.anchor.game;

/**
 * Created by wfx on 21.08.17.
 */

public class Score implements Comparable<Score> {

    String _name;
    int _score;

    public Score(String naam, int score) {
        setName(naam);
        setScore(score);
    }

    public void setName(String name) {
        _name = name;
    }

    public void setScore(int score) {
        _score = score;
    }

    public String getName() {
        return _name;
    }

    public int getScore() {
        return _score;
    }

    public String toString() {
        return "Score:" + getName() + ":" + Integer.toString(getScore());
    }

    public int compareTo(Score cmpScore){
        int score = ((Score) cmpScore).getScore();
        if (getScore()< score) {
            return 1;
        } else if (getScore()>score) {
            return -1;
        } else {
            return 0;
        }
    }
}
