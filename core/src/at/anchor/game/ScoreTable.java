package at.anchor.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.I18NBundle;
import java.util.Arrays;

public class ScoreTable implements Input.TextInputListener {

    public enum State {RequestPlayerName, ShowScores}

    ;

    // State, game and points
    private State _state;
    private EsthetiqueGems _game;
    private I18NBundle _lang;
    private int _points;
    private int _topScorePos;
    private static final int MAX_ENTRIES = 6; // pos 0 to 4 and 5 as buffer
    private static final char DEFAULT_SEPARATOR = ';';

    // Used strings
    String _titleText;

    // Preferences
    Preferences _prefConfig = Gdx.app.getPreferences("config");
    Preferences _prefHighScore = Gdx.app.getPreferences("highscore");

    Score[] _highScore = new Score[MAX_ENTRIES];

    // Positions
    private Vector2 _titlePos;
    private Vector2 _firstScorePos;

    GlyphLayout _layout;

    public ScoreTable(EsthetiqueGems game, int points) {
        // Initial state and game
        _game = game;
        _state = State.RequestPlayerName;
        _points = points;
        _topScorePos = -1;

        // Language
        _lang = new I18NBundle();

        _layout = new GlyphLayout();

        AssetManager assetManager = _game.getAssetManager();
        _lang = assetManager.get("i18n/stateScoreTable", I18NBundle.class);
        _titleText = _lang.get("ScoreTable_Title");

        // Positions
        _layout.setText(_game._fontH1, _titleText);
        _titlePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width) / 2, 652);
        _layout.setText(_game._fontP, _titleText);
        _firstScorePos = new Vector2((EsthetiqueGems.VIRTUAL_WIDTH - _layout.width - 132) / 2, 752);

        // Check scores (new entry...)
        loadScore();
        checkScore();
        _state = State.ShowScores;

    }

    public void draw() {
        SpriteBatch batch = _game.getSpriteBatch();

        if (_state == State.ShowScores) {
            // Render title
            _game._fontH1.draw(batch, _titleText, (int) _titlePos.x, (int) _titlePos.y);

            // Render table
            for (int i = 0; i < MAX_ENTRIES -1; i++) {
                if (i == _topScorePos) {
                    _game._fontPBold.draw(batch, (i+1) + ".  " + _highScore[i].getName(), (int) _firstScorePos.x + 1, (int) _firstScorePos.y + (i * 42));
                    _game._fontPBold.draw(batch, Integer.toString(_highScore[i].getScore()), (int) _firstScorePos.x + 300, (int) _firstScorePos.y + (i * 42));
                } else {
                    _game._fontP.draw(batch, (i+1) + ".  " +_highScore[i].getName(), (int) _firstScorePos.x + 1, (int) _firstScorePos.y + (i * 42));
                    _game._fontP.draw(batch, Integer.toString(_highScore[i].getScore()), (int) _firstScorePos.x + 300, (int) _firstScorePos.y + (i * 42));
                }
            }
        }
    }

    public void checkScore() {
        _topScorePos = -1;
        for (int i = 0; i < MAX_ENTRIES; i++) {

            System.out.println("### checkScore: #" + i + "; " + _points + "; " +_highScore[i].toString());

            if (_points > _highScore[i].getScore()) {
                _topScorePos = i;

                System.out.println("### checkScore: Set top score pos to:" + _topScorePos);

                break;
            }
        }

        if (_topScorePos != -1) {
            _highScore[MAX_ENTRIES -1].setScore(_points);
            String lastName = _prefConfig.getString("lastName", _lang.get("ScoreTable_MissingName"));
            Gdx.input.getTextInput(this, _lang.get("ScoreTable_EnterName"), lastName, "");
        }
    }

    public void loadScore() {
        String strNames = "...............;...............;...............;...............;...............;...............";
        String strScores = "0;0;0;0;0;0";
        String[] topNames = _prefHighScore.getString("topNames", strNames).split(";");
        String[] topScores = _prefHighScore.getString("topScores", strScores).split(";");

        for (int i = 0; i < MAX_ENTRIES; i++) {

            System.out.println("### loadScore: #" + i);
            System.out.println("### topNames :" + topNames[i]);
            System.out.println("### topScores:" + topScores[i]);

            _highScore[i] = new Score(topNames[i], Integer.parseInt(topScores[i]));
        }
    }

    public void saveScore() {
        Arrays.sort(_highScore);
        String topNames = "";
        String topScores = "";
        for (int i = 0; i < MAX_ENTRIES; i++) {
            topNames = topNames + _highScore[i].getName();
            topScores = topScores + Integer.toString(_highScore[i].getScore());
            if (i != MAX_ENTRIES -1) {
                topNames = topNames + DEFAULT_SEPARATOR;
                topScores = topScores + DEFAULT_SEPARATOR;
            }
        }

        System.out.println("### saveScore:");
        System.out.println("### topNames :" + topNames);
        System.out.println("### topScores:" + topScores);

        _prefHighScore.putString("topNames", topNames);
        _prefHighScore.putString("topScores", topScores);
        // if topScorePos == 0
        System.out.println("### topPos :" + _topScorePos);
        System.out.println("### firstName :" + _highScore[0].getName());
        System.out.println("### firstScore:" + _highScore[0].getScore());

        _prefHighScore.putString("firstName", _highScore[0].getName());
        _prefHighScore.putInteger("firstScore", _highScore[0].getScore());
        // fi
        _prefHighScore.flush();

    }

    @Override
    public void canceled() {
        // Show scores
        _state = State.ShowScores;
    }

    @Override
    public void input(String text) {
        // Limitations
        // Max 15 chars
        if (text.length() > 15) {
            text = text.substring(0, 15);
        }
        // Replace any used default seperator char with blank
        text = text.replace(DEFAULT_SEPARATOR, ' ');

        _highScore[MAX_ENTRIES -1].setName(text);
        saveScore();

        System.out.println("### Player name: " + text);

        if (text != _prefConfig.getString("lastName", _lang.get("ScoreTable_MissingName"))) {
            _prefConfig.putString("lastName", text);
            _prefConfig.flush();
        }
    }
}
