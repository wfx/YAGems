package at.anchor.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;

public class StateGame extends State implements GestureDetector.GestureListener {

    public enum State {
        Loading,
        InitialGems,
        Wait,
        SelectedGem,
        ChangingGems,
        WrongMove,
        DisappearingGems,
        FallingGems,
        DisappearingBoard,
        TimeFinished,
        ShowingScoreTable
    }

    //  Current game state
    private State _state;

    //  Loading feedback
    SplashLoad _loading;

    // Fonts
    // _fontH1, _fontP
    private BitmapFont _fontLCD;
    private BitmapFont _fontFloatScore;

    private GlyphLayout _layout;

    //  UI Buttons
    private Button _hintButton;
    private Button _resetButton;
    private Button _exitButton;
    private Button _musicButton;
    //private Button _pauseButton;

    // Textures
    private TextureRegion _imgBackground;
    private TextureRegion _imgBackgroundScoreTable;
    private TextureRegion _imgWhite;
    private TextureRegion _imgRed;
    private TextureRegion _imgPurple;
    private TextureRegion _imgOrange;
    private TextureRegion _imgGreen;
    private TextureRegion _imgYellow;
    private TextureRegion _imgBlue;
    //private TextureRegion _imgSelector;
    private TextureRegion _imgHint;

    // SFX and music
    private Sound _match1SFX;
    private Sound _match2SFX;
    private Sound _match3SFX;
    private Sound _selectSFX;
    private Sound _fallSFX;
    private Music _song;

    // TODO: the same for buttons etc.
    private static final Vector2 gemsInitial = new Vector2(56, 560);

    // Selected squares
    private Coord _selectedSquareFirst;
    private Coord _selectedSquareSecond;

    // Hints
    private double _showingHint;
    private double _animHintTotalTime;
    private Coord _coordHint;

    // Game board
    private Board _board;

    // Animations
    private double _animTime;
    private double _animTotalTime;
    private double _animTotalInitTime;

    // Points and gems matches
    private MultipleMatch _groupedSquares;
    private int _points;
    private int _multiplier = 0;
    private String _txtTime;

    // Starting time
    private double _remainingTime;
    private int _penaltyTime;

    // Floating scores
    private Array<FloatingScore> _floatingScores;

    // Particle effects
    private ParticleEffect _effect;
    private ParticleEffectPool _effectPool;
    private Array<ParticleEffectPool.PooledEffect> _effects;

    // Mouse pos
    private Vector3 _mousePos = null;

    // Scores table
    private ScoreTable _scoreTable;

    // Aux variables
    private Color _imgColor = Color.WHITE.cpy();
    private Coord _coord = new Coord();

    private GestureDetector gestureDetector;

    public StateGame(EsthetiqueGems esthetiqueGems) {
        super(esthetiqueGems);

        // Initial state
        _state = State.Loading;

        _loading = new SplashLoad(_parent, "Loading_Game");

        // Resources are initially null
        _imgBackground = null;
        _imgBackgroundScoreTable = null;

        // Mouse pos
        _mousePos = new Vector3();

        _txtTime = "";

        _floatingScores = new Array<FloatingScore>();

        _layout = new GlyphLayout();

        _board = new Board();
        _selectedSquareFirst = new Coord(-1, -1);
        _selectedSquareSecond = new Coord(-1, -1);

        // Particle effects
        _effect = new ParticleEffect();
        _effect.load(Gdx.files.internal("data/particleStars"), _parent.getAtlasParticle());
        _effectPool = new ParticleEffectPool(_effect, 20, 100);
        _effects = new Array<ParticleEffectPool.PooledEffect>();

        // Init game for the first time
        init();
    }

    @Override
    public void load() {
        // LCD Font are only here in use.
        // TODO: Clean out; clean way to assigning the font resource at loading?
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font/lcd.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        fontParameter.size = 70;
        fontParameter.genMipMaps = true;
        fontParameter.magFilter = Texture.TextureFilter.Linear;
        fontParameter.minFilter = Texture.TextureFilter.Linear;
        fontParameter.color = new Color(0f, 0f, 0f, 1f);
        fontParameter.shadowColor = new Color(0.8f, 0.8f, 0.8f, 0.6f);
        fontParameter.shadowOffsetY = 3;
        fontParameter.flip = true;
        fontParameter.characters = "1234567890:";
        _fontLCD = fontGenerator.generateFont(fontParameter);
        _fontLCD.setUseIntegerPositions(false);
        fontParameter.color = Color.GOLD;
        _fontFloatScore = fontGenerator.generateFont(fontParameter);
        _fontFloatScore.setUseIntegerPositions(false);
        fontGenerator.dispose();

        AssetManager assetManager = _parent.getAssetManager();
        assetManager.load("i18n/stateScoreTable", I18NBundle.class);
        assetManager.load("img/stateGameBg.png", Texture.class);
        assetManager.load("img/stateGameScoreTableBg.png", Texture.class);
        assetManager.load("audio/match1.ogg", Sound.class);
        assetManager.load("audio/match2.ogg", Sound.class);
        assetManager.load("audio/match3.ogg", Sound.class);
        assetManager.load("audio/select.ogg", Sound.class);
        assetManager.load("audio/fall.ogg", Sound.class);
        assetManager.load("audio/music1.ogg", Music.class);

        // TODO: Reset game parameters at loading?
        resetGame();
    }

    @Override
    public void unload() {

        // Set references to null
        _imgBackground = null;
        _imgBackgroundScoreTable = null;
        _hintButton.setNull();
        _resetButton.setNull();
        _exitButton.setNull();
        _musicButton.setNull();
        //_pauseButton.setNull();
        _imgWhite = null;
        _imgRed = null;
        _imgPurple = null;
        _imgOrange = null;
        _imgGreen = null;
        _imgYellow = null;
        _imgBlue = null;
        //_imgSelector = null;
        _match1SFX = null;
        _match2SFX = null;
        _match3SFX = null;
        _selectSFX = null;
        _fallSFX = null;
        _song = null;
        //_lang = null;
        _fontLCD.dispose();
        _fontFloatScore.dispose();

        // Unload assets
        AssetManager assetManager = _parent.getAssetManager();
        assetManager.unload("i18n/stateScoreTable");
        assetManager.unload("img/stateGameBg.png");
        assetManager.unload("img/stateGameScoreTableBg.png");
        assetManager.unload("audio/match1.ogg");
        assetManager.unload("audio/match2.ogg");
        assetManager.unload("audio/match3.ogg");
        assetManager.unload("audio/select.ogg");
        assetManager.unload("audio/fall.ogg");
        assetManager.unload("audio/music1.ogg");

    }

    @Override
    public void assignResources() {
        super.assignResources();

        AssetManager assetManager = _parent.getAssetManager();

        // Button EXIT
        _exitButton = new Button(_parent, 56, 330);
        _exitButton.setNormal(_parent.getAtlasButtons().findRegion("btnExit"), _parent.getAtlasButtons().findRegion("btnExitClicked"));
        // Button Reset/Restart
        _resetButton = new Button(_parent, 196, 330);
        _resetButton.setNormal(_parent.getAtlasButtons().findRegion("btnRestart"), _parent.getAtlasButtons().findRegion("btnRestartClicked"));
        // Button Music On/Off
        _musicButton = new Button(_parent, 296, 330);
        _musicButton.setNormal(_parent.getAtlasButtons().findRegion("btnMusicOff"), _parent.getAtlasButtons().findRegion("btnMusicOffClicked"));
        _musicButton.setSwitch(_parent.getAtlasButtons().findRegion("btnMusicOn"), _parent.getAtlasButtons().findRegion("btnMusicOnClicked"));
        _musicButton.setModeOn(false);

        // Button Hint
        _hintButton = new Button(_parent, 580, 330);
        _hintButton.setNormal(_parent.getAtlasButtons().findRegion("btnHint"), _parent.getAtlasButtons().findRegion("btnHintClicked"));

        _imgBackground = new TextureRegion(assetManager.get("img/stateGameBg.png", Texture.class));
        _imgBackground.flip(false, true);

        _imgBackgroundScoreTable = new TextureRegion(assetManager.get("img/stateGameScoreTableBg.png", Texture.class));
        _imgBackgroundScoreTable.flip(false, true);

        _imgHint = new TextureRegion(_parent.getAtlasUI().findRegion("hint"));
        // flipping make no sense

        _imgWhite = new TextureRegion(_parent.getAtlasGems().findRegion("gemWhite"));
        _imgWhite.flip(false, true);

        _imgRed = new TextureRegion(_parent.getAtlasGems().findRegion("gemRed"));
        _imgRed.flip(false, true);

        _imgPurple = new TextureRegion(_parent.getAtlasGems().findRegion("gemPurple"));
        _imgPurple.flip(false, true);

        _imgOrange = new TextureRegion(_parent.getAtlasGems().findRegion("gemOrange"));
        _imgOrange.flip(false, true);

        _imgGreen = new TextureRegion(_parent.getAtlasGems().findRegion("gemGreen"));
        _imgGreen.flip(false, true);

        _imgYellow = new TextureRegion(_parent.getAtlasGems().findRegion("gemYellow"));
        _imgYellow.flip(false, true);

        _imgBlue = new TextureRegion(_parent.getAtlasGems().findRegion("gemBlue"));
        _imgBlue.flip(false, true);

        _match1SFX = assetManager.get("audio/match1.ogg", Sound.class);
        _match2SFX = assetManager.get("audio/match2.ogg", Sound.class);
        _match3SFX = assetManager.get("audio/match3.ogg", Sound.class);
        _selectSFX = assetManager.get("audio/select.ogg", Sound.class);
        _fallSFX = assetManager.get("audio/fall.ogg", Sound.class);
        _song = assetManager.get("audio/music1.ogg", Music.class);

        //Gdx.input.setInputProcessor(this);

        gestureDetector = new GestureDetector(20, 0.5f, 2, 0.15f, this);
        Gdx.input.setInputProcessor(gestureDetector);

    }

    @Override
    public void update(double deltaT) {

        // Update mouse pos
        _mousePos.x = Gdx.input.getX();
        _mousePos.y = Gdx.input.getY();
        _parent.getCamera().unproject(_mousePos);

        // LOADING STATE
        if (_state == State.Loading) {
            // If we finish loading, assign resources and change to FirstFlip state
            if (_parent.getAssetManager().update()) {
                assignResources();
                _state = State.InitialGems;
            }

            return;
        }

        // Particle effects
        int numParticles = _effects.size;

        for (int i = 0; i < numParticles; ++i) {
            _effects.get(i).update(Gdx.graphics.getDeltaTime());
        }

        // Game time
        _remainingTime -= deltaT;
        // If we are under the time limit, compute the string for the board
        if (_remainingTime > 0) {
            int minutes = (int) (_remainingTime / 60.0);
            int seconds = (int) (_remainingTime - minutes * 60);
            _txtTime = "" + minutes;
            if (seconds < 10) {
                _txtTime += ":0" + seconds;
            } else {
                _txtTime += ":" + seconds;
            }
        }

        // If we are over the time limit and not in a final score
        else if (_remainingTime <= 0 && _state != State.TimeFinished && _state != State.ShowingScoreTable) {
            // End the game
            _state = State.TimeFinished;

            // Take gems out of screen
            gemsOutScreen();
        }

        // Remove the hidden floating score
        removeEndedFloatingScores();

        // Remove the ended particle systems
        removeEndedParticles();

        // INITIAL GAME STATE
        if (_state == State.InitialGems) {
            // If animation ended
            if ((_animTime += deltaT) >= _animTotalInitTime) {
                // Switch to next state (waiting for user input)
                _state = State.Wait;
                _board.endAnimation();

                // Reset animation step counter
                _animTime = 0;
            }
        }

        // WAITING STATE
        if (_state == State.Wait) {
            // Multiplier must be 0
            _multiplier = 0;
        }

        // SWAPPING GEMS STATE
        if (_state == State.ChangingGems) {
            // When animation ends
            if ((_animTime += deltaT) >= _animTotalTime) {
                // Switch to next state, gems start to disappear
                _state = State.DisappearingGems;

                // Swap gems in the board
                _board.swap(_selectedSquareFirst.x,
                        _selectedSquareFirst.y,
                        _selectedSquareSecond.x,
                        _selectedSquareSecond.y);

                // Increase multiplier
                ++_multiplier;

                // Play matching sounds
                playMatchSound();

                // Create floating scores for the matching group
                createFloatingScores();

                // Reset animation step
                _animTime = 0;

            }
        }

        // DISAPPEARING GEMS STATE
        if (_state == State.DisappearingGems) {

            // When anim ends
            if ((_animTime += deltaT) >= _animTotalTime) {
                // Switch to next state, gems falling
                _state = State.FallingGems;

                // Redraw scoreboard with new points
                redrawScoreBoard();

                // Delete squares that were matched on the board
                for (int i = 0; i < _groupedSquares.size; ++i) {
                    for (int j = 0; j < _groupedSquares.get(i).size; ++j) {
                        _board.del(_groupedSquares.get(i).get(j).x,
                                _groupedSquares.get(i).get(j).y);
                    }
                }

                // Calculate fall movements
                _board.calcFallMovements();

                // Apply changes to the board
                _board.applyFall();

                // Fill empty spaces
                _board.fillSpaces();

                // Reset animation counter
                _animTime = 0;
            }
        }

        // GEMS FALLING STATE
        if (_state == State.FallingGems) {
            // When animation ends
            if ((_animTime += deltaT) >= _animTotalTime) {
                // Play the fall sound fx
                _fallSFX.play();

                // Switch to the next state (waiting)
                _state = State.Wait;

                // Reset animation counter
                _animTime = 0;

                // Reset animation variables
                _board.endAnimation();

                // Check if there are matching groups
                _groupedSquares = _board.check();

                // If there are...
                if (_groupedSquares.size != 0) {
                    // Increase the score multiplier
                    ++_multiplier;

                    // Create the floating scores
                    createFloatingScores();

                    // Play matching sound
                    playMatchSound();

                    // Go back to the gems-fading state
                    _state = State.DisappearingGems;
                }

                // If there are neither current solutions nor possible future solutions
                else if (_board.solutions().size == 0) {
                    // Make the board disappear
                    _state = State.DisappearingBoard;
                    gemsOutScreen();
                }
            }
        }

        // DISAPPEARING BOARD STATE because there were no possible movements
        else if (_state == State.DisappearingBoard) {
            // When animation ends
            if ((_animTime += deltaT) >= _animTotalTime) {
                // Switch to the initial state
                _state = State.InitialGems;

                // Generate a brand new board
                _board.generate();

                // Reset animation counter
                _animTime = 0;
            }
        }

        // In this state, the time has finished, so we need to create a ScoreBoard
        else if (_state == State.TimeFinished) {

            // When animation ends
            if ((_animTime += deltaT) >= _animTotalInitTime) {

                // Create a new score table
                _scoreTable = new ScoreTable(_parent, _points);

                // Switch to the following state
                _state = State.ShowingScoreTable;

                // Reset animation counter
                _animTime = 0;
            }
        }

        // Whenever a hint is being shown, decrease its controlling variable
        if (_showingHint > 0.0) {
            _showingHint -= Gdx.graphics.getDeltaTime();
        }
    }

    private void removeEndedParticles() {
        int numParticles = _effects.size;

        for (int i = 0; i < numParticles; ++i) {
            ParticleEffectPool.PooledEffect effect = _effects.get(i);

            if (effect.isComplete()) {
                _effectPool.free(effect);
                _effects.removeIndex(i);
                --i;
                --numParticles;
            }
        }
    }

    private void removeEndedFloatingScores() {
        int numScores = _floatingScores.size;

        for (int i = 0; i < numScores; ++i) {
            if (_floatingScores.get(i).isFinished()) {
                _floatingScores.removeIndex(i);
                --i;
                --numScores;
            }
        }
    }

    @Override
    public void render() {
        SpriteBatch batch = _parent.getSpriteBatch();

        // STATE LOADING - Just render loading
        if (_state == State.Loading) {
            _loading.draw();
            return;
        }

        // Background image: Game or Score table
        if (_scoreTable != null && _state == State.ShowingScoreTable) {
            batch.draw(_imgBackgroundScoreTable, 0, 0);
        } else {
            batch.draw(_imgBackground, 0, 0);
        }

        // Draw buttons
        _hintButton.render();
        _resetButton.render();
        _musicButton.render();
        _exitButton.render();
        // TODO: Adding pause/resume (?)
        // _pauseButton.render();


        // Draw board
        TextureRegion img = null;

        if (_state != State.ShowingScoreTable) {
            // Go through all of the squares
            for (int i = 0; i < 8; ++i) {
                for (int j = 0; j < 8; ++j) {

                    // Check the type of each square and
                    // save the proper image in the img pointer
                    switch (_board.getSquare(i, j).getType()) {
                        case sqWhite:
                            img = _imgWhite;
                            break;

                        case sqRed:
                            img = _imgRed;
                            break;

                        case sqPurple:
                            img = _imgPurple;
                            break;

                        case sqOrange:
                            img = _imgOrange;
                            break;

                        case sqGreen:
                            img = _imgGreen;
                            break;

                        case sqYellow:
                            img = _imgYellow;
                            break;

                        case sqBlue:
                            img = _imgBlue;
                            break;
                        default:
                            break;

                    } // switch end

                    // Now, if img is not NULL (there's something to draw)
                    if (img != null) {
                        // Default positions
                        float imgX = gemsInitial.x + i * 76;
                        float imgY = gemsInitial.y + j * 76;

                        // In the initial state, the gems fall vertically
                        // decreasing its speed
                        if (_state == State.InitialGems) {
                            imgY = Animation.easeOutQuad(_animTime,
                                    gemsInitial.y + _board.getSquares()[i][j].origY * 76,
                                    _board.getSquares()[i][j].destY * 76,
                                    _animTotalInitTime);
                        }

                        // In the ending states, gems fall vertically,
                        // increasing their speed
                        else if (_state == State.DisappearingBoard || _state == State.TimeFinished) {
                            imgY = Animation.easeInQuad(_animTime,
                                    gemsInitial.y + _board.getSquares()[i][j].origY * 76,
                                    _board.getSquares()[i][j].destY * 76,
                                    _animTotalInitTime);
                        } else if ((_state == State.Wait ||
                                _state == State.SelectedGem ||
                                _state == State.FallingGems)
                                && _board.getSquare(i, j).mustFall) {

                            imgY = Animation.easeOutQuad(_animTime,
                                    gemsInitial.y + _board.getSquares()[i][j].origY * 76,
                                    _board.getSquares()[i][j].destY * 76,
                                    _animTotalTime);
                        }

                        else if (_state == State.WrongMove) {
                            _state = State.Wait;
                            System.out.println("WRONG MOVE");
                        }

                        // When two gems are switching
                        else if (_state == State.ChangingGems) {
                            if (i == _selectedSquareFirst.x && j == _selectedSquareFirst.y) {

                                imgX = Animation.easeOutQuad(_animTime,
                                        gemsInitial.x + i * 76,
                                        (_selectedSquareSecond.x - _selectedSquareFirst.x) * 76,
                                        _animTotalTime);

                                imgY = Animation.easeOutQuad(_animTime,
                                        gemsInitial.y + j * 76,
                                        (_selectedSquareSecond.y - _selectedSquareFirst.y) * 76,
                                        _animTotalTime);

                            } else if (i == _selectedSquareSecond.x && j == _selectedSquareSecond.y) {

                                imgX = Animation.easeOutQuad(_animTime,
                                        gemsInitial.x + i * 76,
                                        (_selectedSquareFirst.x - _selectedSquareSecond.x) * 76,
                                        _animTotalTime);

                                imgY = Animation.easeOutQuad(_animTime,
                                        gemsInitial.y + j * 76,
                                        (_selectedSquareFirst.y - _selectedSquareSecond.y) * 76,
                                        _animTotalTime);
                            }
                        } else if (_state == State.DisappearingGems) {
                            // Winning gems disappearing
                            if (_groupedSquares.isMatched(new Coord(i, j))) {
                                _imgColor.a = 1.0f - (float) (_animTime / _animTotalTime);
                            }
                        }

                        // Finally draw the image
                        batch.setColor(_imgColor);
                        batch.draw(img, imgX, imgY);
                        _imgColor.a = 1.0f;
                        batch.setColor(_imgColor);

                    } // End if (img != NULL)

                    img = null;
                }
            }

            // If a hint is being shown
            if (_showingHint > 0.0) {

                float x = gemsInitial.x + _coordHint.x * 76;
                float y = gemsInitial.y + _coordHint.y * 76;

                batch.draw(_imgHint, x, y);

            }
        }

        _layout.setText(_fontLCD, Integer.toString(_points));
        _fontLCD.draw(batch, Integer.toString(_points), 648 - _layout.width, 468);

        _layout.setText(_fontLCD, _txtTime);
        _fontLCD.draw(batch, _txtTime, 264 - _layout.width, 468);

        // Score table
        if (_scoreTable != null && _state == State.ShowingScoreTable) {
            _scoreTable.draw();
        }

        // Draw each score little message
        int numScores = _floatingScores.size;

        for (int i = 0; i < numScores; ++i) {
            _floatingScores.get(i).draw();
        }

        // Draw particle systems
        int numParticles = _effects.size;

        for (int i = 0; i < numParticles; ++i) {
            _effects.get(i).draw(batch);
        }

    }

    @Override
    public boolean keyDown(int arg0) {
        if (arg0 == Input.Keys.BACK) {
            _parent.changeState("StateMenu");
        }

        return false;
    }


    @Override
    public boolean keyUp(int arg0) {
        return false;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        // Todo use lib gdx button!
        /*
        _hintButton.touchUp();
        _musicButton.touchUp();
        _exitButton.touchUp();
        _resetButton.touchUp();
        //_pauseButton.touchUp();
        */
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {

        if (button == 0) { // Left mouse button clicked
            _mousePos.x = x;
            _mousePos.y = y;
            _parent.getCamera().unproject(_mousePos);

            // Button
            if (_exitButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                _parent.changeState("StateMenu");
            } else if (_hintButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                showHint();
                // Penalty
                if (_remainingTime - _remainingTime / 10 > 0) {
                    _remainingTime -= _remainingTime / 10;
                }
                if (_points > _remainingTime) {
                    _points -= _remainingTime;
                } else {
                    _points = 0;
                }
            } else if (_musicButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                if (_song.isPlaying()) {
                    // Turn off music
                    _song.stop();
                    _musicButton.setModeOn(false);
                } else {
                    // Turn on music
                    _song.setLooping(true);
                    _song.play();
                    _musicButton.setModeOn(true);
                }
                _musicButton.touchUp();
            } else if (_resetButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                _state = State.DisappearingBoard;
                gemsOutScreen();
                resetGame();
                _resetButton.touchUp();
            } /* else if (_pauseButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                if (_state == State.Wait) {
                    // PAUSE
                } else {
                    // RESUME
                }
            } */
        }

        return true;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        _mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        _parent.getCamera().unproject(_mousePos);

        if (overGem((int) _mousePos.x, (int) _mousePos.y)) {
            if (_state == State.Wait) {
                _state = State.SelectedGem;
                Coord coord = getCoord((int) _mousePos.x, (int) _mousePos.y);
                _selectedSquareFirst.x = coord.x;
                _selectedSquareFirst.y = coord.y;
                if (Math.abs(velocityX) > Math.abs(velocityY)) {
                    if (velocityX > 0) {
                        // right 2 left < < < <
                        if (!checkClickedSquare(_selectedSquareFirst.x - 1, _selectedSquareFirst.y)) {
                            _selectedSquareFirst.x = -1;
                            _selectedSquareFirst.y = -1;
                            _state = State.Wait;
                        }
                    } else {
                        if (velocityX < 0) {
                            // left 2 right > > > >
                            if (!checkClickedSquare(_selectedSquareFirst.x + 1, _selectedSquareFirst.y)) {
                                _selectedSquareFirst.x = -1;
                                _selectedSquareFirst.y = -1;
                                _state = State.Wait;
                            }
                        }
                    }
                } else {
                    if (velocityY > 0) {
                        // top 2 bottom v v v v
                        --_selectedSquareFirst.y;
                        if (!checkClickedSquare(_selectedSquareFirst.x, _selectedSquareFirst.y + 1)) {
                            _selectedSquareFirst.x = -1;
                            _selectedSquareFirst.y = -1;
                            _state = State.Wait;
                        }
                    } else {
                        if (velocityY < 0) {
                            // bottom 2 top ^ ^ ^ ^
                            ++_selectedSquareFirst.y;
                            if (!checkClickedSquare(_selectedSquareFirst.x, _selectedSquareFirst.y - 1)) {
                                _selectedSquareFirst.x = -1;
                                _selectedSquareFirst.y = -1;
                                _state = State.Wait;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }


    private void gemsOutScreen() {
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 8; ++y) {
                _board.getSquare(x, y).mustFall = true;
                _board.getSquare(x, y).origY = y;
                _board.getSquare(x, y).destY = 9 + MathUtils.random(1, 7);
            }
        }
    }

    private void init() {
        // Initial animation state
        _animTime = 0;

        // Steps for short animations
        _animTotalTime = 0.3;

        // Steps for long animations
        _animTotalInitTime = 1.0;

        // Steps for the hint animation
        _animHintTotalTime = 2.0;

        // Reset the hint flag
        _showingHint = -1;

        // Initial score multiplier
        _multiplier = 1;

        // Reset the game to the initial values
        resetGame();
    }

    private boolean overGem(int mX, int mY) {
        return (mX > gemsInitial.x && mX < gemsInitial.x + 76 * 8 &&
                mY > gemsInitial.y && mY < gemsInitial.y + 76 * 8);
    }

    private Coord getCoord(int mX, int mY) {
        _coord.x = (mX - (int) gemsInitial.x) / 76;
        _coord.y = (mY - (int) gemsInitial.y) / 76;
        return _coord;
    }

    private void redrawScoreBoard() {

    }

    private void createFloatingScores() {
        // For each match in the group of matched squares
        int numMatches = _groupedSquares.size;

        for (int i = 0; i < numMatches; ++i) {
            // Create new floating score
            Match match = _groupedSquares.get(i);
            int matchSize = match.size;
            _floatingScores.add(new FloatingScore(_parent,
                    _fontFloatScore,
                    matchSize * (5 * _penaltyTime),
                    gemsInitial.x + match.getMidSquare().x * 76 + 5,
                    gemsInitial.y + match.getMidSquare().y * 76 + 5));

            // Create a particle effect for each matching square
            for (int j = 0; j < matchSize; ++j) {
                ParticleEffectPool.PooledEffect newEffect = _effectPool.obtain();
                newEffect.setPosition(gemsInitial.x + match.get(j).x * 76 + 38, gemsInitial.y + match.get(j).y * 76 + 38);
                newEffect.start();
                _effects.add(newEffect);
            }

            // Difficulties
            _remainingTime += matchSize;
            if (_points >= 500) {
                _penaltyTime = 2;
            }
            if (_points >= 1500) {
                _penaltyTime = 3;
            }
            if (_points >= 2500) {
                _penaltyTime = 4;
            }
            _points += matchSize * (5 * _penaltyTime);
        }
        //System.out.println("### T1: " + (_remainingTime) + " ###");
        if (_remainingTime + _multiplier - _penaltyTime > 0) {
            _remainingTime += _multiplier - _penaltyTime;
        }
        //System.out.println("### PT: " + _penaltyTime + " ###");
        //System.out.println("### T2: " + (_remainingTime + _multiplier - _penaltyTime) + " ###");
    }

    private boolean checkClickedSquare(int mX, int mY) {
        _selectedSquareSecond.x = mX;
        _selectedSquareSecond.y = mY;

        // If gem is neighbour
        if (Math.abs(_selectedSquareFirst.x - _selectedSquareSecond.x)
                + Math.abs(_selectedSquareFirst.y - _selectedSquareSecond.y) == 1) {

            _board.swap(_selectedSquareFirst.x, _selectedSquareFirst.y,
                    _selectedSquareSecond.x, _selectedSquareSecond.y);

            _groupedSquares = _board.check();

            // If winning movement
            if (_groupedSquares.size != 0) {
                _state = State.ChangingGems;

                _selectSFX.play();

                _board.swap(_selectedSquareFirst.x, _selectedSquareFirst.y,
                        _selectedSquareSecond.x, _selectedSquareSecond.y);

                return true;
            } else {
                _state = State.WrongMove;
            }
            _board.swap(_selectedSquareFirst.x, _selectedSquareFirst.y,
                    _selectedSquareSecond.x, _selectedSquareSecond.y);
        }
        return false;
    }

    private void showHint() {
        Array<Coord> solutions = _board.solutions();
        _coordHint = solutions.get(0);
        _showingHint = _animHintTotalTime;

        _hintButton.touchUp();
    }

    private void playMatchSound() {
        if (_multiplier == 1) {
            _match1SFX.play();
        } else if (_multiplier == 2) {
            _match2SFX.play();
        } else {
            _match3SFX.play();
        }
    }

    private void resetGame() {
        // Reset score
        _points = 0;

        // Generate board
        _board.generate();

        // Redraw the scoreboard
        redrawScoreBoard();

        // Restart the time (two minutes)
        _remainingTime = 30;
        _penaltyTime = 1;

    }

    @Override
    public void resume() {
        _state = State.Loading;
    }
}