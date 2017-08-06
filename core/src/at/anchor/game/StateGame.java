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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;

public class StateGame extends State {

    public enum State {
        Loading,
        InitialGems,
        Wait,
        SelectedGem,
        ChangingGems,
        DisappearingGems,
        FallingGems,
        DisappearingBoard,
        TimeFinished,
        ShowingScoreTable
    }

    //  Current game state
    private State _state;

    //  Internationalizing
    private I18NBundle _lang;

    //  Loading feedback
    SplashLoad _loading;

    // Fonts
    // _fontH1, _fontP
    private BitmapFont _fontLCD;

    private GlyphLayout _layout;

    //  UI Buttons
    private Button _hintButton;
    private Button _resetButton;
    private Button _exitButton;
    private Button _musicButton;
    private Button _pauseButton;

    // Textures
    private TextureRegion _imgBackground;
    private TextureRegion _imgWhite;
    private TextureRegion _imgRed;
    private TextureRegion _imgPurple;
    private TextureRegion _imgOrange;
    private TextureRegion _imgGreen;
    private TextureRegion _imgYellow;
    private TextureRegion _imgBlue;
    private TextureRegion _imgSelector;
    private TextureRegion _imgHint;

    // SFX and music
    private Sound _match1SFX;
    private Sound _match2SFX;
    private Sound _match3SFX;
    private Sound _selectSFX;
    private Sound _fallSFX;
    private Music _song;

    private static final Vector2 gemsInitial = new Vector2(56, 622);

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

    // Background textures
    private TextureRegion _imgScoreBackground;
    private TextureRegion _imgTimeBackground;

    // Starting time
    private double _remainingTime;
    private double _penaltyTime;

    // Floating scores
    // TODO: Top 5
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

    public StateGame(EsthetiqueGems esthetiqueGems) {
        super(esthetiqueGems);

        // Language
        _lang = new I18NBundle();
        _loading = new SplashLoad(_parent, "Loading_Game");

        // Initial state
        _state = State.Loading;

        // Create buttons
        _exitButton = new Button(_parent, 56, 394, "");
        _resetButton = new Button(_parent, 186, 394, "");
        _musicButton = new Button(_parent, 316, 394, "");
        _hintButton = new Button(_parent, 576, 394, "");
        _pauseButton = new Button(_parent, 446, 394, "");

        // Creare board
        _board = new Board();

        // Time txt
        _txtTime = "";

        _selectedSquareFirst = new Coord(-1, -1);
        _selectedSquareSecond = new Coord(-1, -1);

        // Scores
        _floatingScores = new Array<FloatingScore>();

        // Mouse pos
        _mousePos = new Vector3();

        // Particle effects
        _effect = new ParticleEffect();
        _effect.load(Gdx.files.internal("data/particleStars"), Gdx.files.internal("img"));
        _effectPool = new ParticleEffectPool(_effect, 20, 100);

        _effects = new Array<ParticleEffectPool.PooledEffect>();

        _layout = new GlyphLayout();

        // Init game for the first time
        init();
    }

    @Override
    public void load() {
        AssetManager assetManager = _parent.getAssetManager();

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
        _fontLCD = fontGenerator.generateFont(fontParameter);
        _fontLCD.setUseIntegerPositions(false);
        fontGenerator.dispose();

        assetManager.load("i18n/stateGame", I18NBundle.class);
        assetManager.load("i18n/stateScoreTable", I18NBundle.class);
        assetManager.load("img/scoreBackground.png", Texture.class);
        assetManager.load("img/btnBg.png", Texture.class);
        assetManager.load("img/btnClickedBg.png", Texture.class);
        assetManager.load("img/stateGameBg.png", Texture.class);
        assetManager.load("img/selector.png", Texture.class);
        assetManager.load("img/hint.png", Texture.class);
        assetManager.load("img/timeBg.png", Texture.class);
        assetManager.load("img/gemWhite.png", Texture.class);
        assetManager.load("img/gemRed.png", Texture.class);
        assetManager.load("img/gemPurple.png", Texture.class);
        assetManager.load("img/gemOrange.png", Texture.class);
        assetManager.load("img/gemGreen.png", Texture.class);
        assetManager.load("img/gemYellow.png", Texture.class);
        assetManager.load("img/gemBlue.png", Texture.class);
        assetManager.load("img/iconHint.png", Texture.class);
        assetManager.load("img/iconRestart.png", Texture.class);
        assetManager.load("img/iconExit.png", Texture.class);
        assetManager.load("img/iconMusicOn.png", Texture.class);
        assetManager.load("img/iconMusicOff.png", Texture.class);
        assetManager.load("img/iconPause.png", Texture.class);
        assetManager.load("audio/match1.ogg", Sound.class);
        assetManager.load("audio/match2.ogg", Sound.class);
        assetManager.load("audio/match3.ogg", Sound.class);
        assetManager.load("audio/select.ogg", Sound.class);
        assetManager.load("audio/fall.ogg", Sound.class);
        assetManager.load("audio/music1.ogg", Music.class);

        resetGame();
    }

    @Override
    public void unload() {
        // Set assets references to null
        _lang = null;
        _imgBackground = null;
        _imgWhite = null;
        _imgRed = null;
        _imgPurple = null;
        _imgOrange = null;
        _imgGreen = null;
        _imgYellow = null;
        _imgBlue = null;
        _imgSelector = null;
        _imgScoreBackground = null;
        _imgTimeBackground = null;
        _match1SFX = null;
        _match2SFX = null;
        _match3SFX = null;
        _selectSFX = null;
        _fallSFX = null;
        _song = null;

        _hintButton.setIcon(null);
        _resetButton.setIcon(null);
        _exitButton.setIcon(null);
        _musicButton.setIcon(null);
        _pauseButton.setIcon(null);
        _pauseButton.setIconClicked(null);

        _hintButton.setBackground(null);
        _resetButton.setBackground(null);
        _exitButton.setBackground(null);
        _musicButton.setBackground(null);
        _pauseButton.setBackground(null);

        // Unload assets
        AssetManager assetManager = _parent.getAssetManager();

        assetManager.unload("i18n/stateGame");
        assetManager.unload("i18n/stateScoreTable");
        assetManager.unload("img/scoreBackground.png");
        assetManager.unload("img/btnBg.png");
        assetManager.unload("img/btnClickedBg.png");
        assetManager.unload("img/stateGameBg.png");
        assetManager.unload("img/selector.png");
        assetManager.unload("img/hint.png");
        assetManager.unload("img/timeBg.png");
        assetManager.unload("img/gemWhite.png");
        assetManager.unload("img/gemRed.png");
        assetManager.unload("img/gemPurple.png");
        assetManager.unload("img/gemOrange.png");
        assetManager.unload("img/gemGreen.png");
        assetManager.unload("img/gemYellow.png");
        assetManager.unload("img/gemBlue.png");
        assetManager.unload("img/iconHint.png");
        assetManager.unload("img/iconRestart.png");
        assetManager.unload("img/iconExit.png");
        assetManager.unload("img/iconMusicOn.png");
        assetManager.unload("img/iconMusicOff.png");
        assetManager.unload("img/iconPause.png");
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

        // Load textures
        _imgScoreBackground = new TextureRegion(assetManager.get("img/scoreBackground.png", Texture.class));
        _imgBackground = new TextureRegion(assetManager.get("img/stateGameBg.png", Texture.class));
        _imgSelector = new TextureRegion(assetManager.get("img/selector.png", Texture.class));
        _imgHint = new TextureRegion(assetManager.get("img/hint.png", Texture.class));
        _imgTimeBackground = new TextureRegion(assetManager.get("img/timeBg.png", Texture.class));
        _imgWhite = new TextureRegion(assetManager.get("img/gemWhite.png", Texture.class));
        _imgRed = new TextureRegion(assetManager.get("img/gemRed.png", Texture.class));
        _imgPurple = new TextureRegion(assetManager.get("img/gemPurple.png", Texture.class));
        _imgOrange = new TextureRegion(assetManager.get("img/gemOrange.png", Texture.class));
        _imgGreen = new TextureRegion(assetManager.get("img/gemGreen.png", Texture.class));
        _imgYellow = new TextureRegion(assetManager.get("img/gemYellow.png", Texture.class));
        _imgBlue = new TextureRegion(assetManager.get("img/gemBlue.png", Texture.class));

        _imgScoreBackground.flip(false, true);
        _imgBackground.flip(false, true);
        _imgSelector.flip(false, true);
        _imgTimeBackground.flip(false, true);
        _imgWhite.flip(false, true);
        _imgRed.flip(false, true);
        _imgPurple.flip(false, true);
        _imgOrange.flip(false, true);
        _imgGreen.flip(false, true);
        _imgYellow.flip(false, true);
        _imgBlue.flip(false, true);

        // Language bundle
        _lang = assetManager.get("i18n/stateGame", I18NBundle.class);

        // Button textures and font
        TextureRegion buttonBackground = new TextureRegion(assetManager.get("img/btnBg.png", Texture.class));
        TextureRegion buttonBackgroundClicked = new TextureRegion(assetManager.get("img/btnClickedBg.png", Texture.class));
        TextureRegion iconHint = new TextureRegion(assetManager.get("img/iconHint.png", Texture.class));
        TextureRegion iconRestart = new TextureRegion(assetManager.get("img/iconRestart.png", Texture.class));
        TextureRegion iconExit = new TextureRegion(assetManager.get("img/iconExit.png", Texture.class));
        TextureRegion iconMusicOn = new TextureRegion(assetManager.get("img/iconMusicOn.png", Texture.class));
        TextureRegion iconMusicOff = new TextureRegion(assetManager.get("img/iconMusicOff.png", Texture.class));
        TextureRegion iconPause = new TextureRegion(assetManager.get("img/iconPause.png", Texture.class));

        buttonBackground.flip(false, true);
        iconHint.flip(false, true);
        iconRestart.flip(false, true);
        iconExit.flip(false, true);
        iconMusicOn.flip(false, true);
        iconMusicOff.flip(false,true);
        iconPause.flip(false, true);

        _hintButton.setIcon(iconHint);
        _resetButton.setIcon(iconRestart);
        _exitButton.setIcon(iconExit);
        _musicButton.setIcon(iconMusicOn);
        _pauseButton.setIcon(iconPause);

        _musicButton.setIconClicked(iconMusicOff);

        _hintButton.setBackground(buttonBackground);
        _resetButton.setBackground(buttonBackground);
        _exitButton.setBackground(buttonBackground);
        _musicButton.setBackground(buttonBackground);
        _pauseButton.setBackground(buttonBackground);

        _hintButton.setBackgroundClicked(buttonBackgroundClicked);
        _resetButton.setBackgroundClicked(buttonBackgroundClicked);
        _exitButton.setBackgroundClicked(buttonBackgroundClicked);
        _musicButton.setBackgroundClicked(buttonBackgroundClicked);
        _pauseButton.setBackgroundClicked(buttonBackgroundClicked);

        // Load SFX and music
        _match1SFX = assetManager.get("audio/match1.ogg", Sound.class);
        _match2SFX = assetManager.get("audio/match2.ogg", Sound.class);
        _match3SFX = assetManager.get("audio/match3.ogg", Sound.class);
        _selectSFX = assetManager.get("audio/select.ogg", Sound.class);
        _fallSFX = assetManager.get("audio/fall.ogg", Sound.class);
        _song = assetManager.get("audio/music1.ogg", Music.class);

        Gdx.input.setInputProcessor(this);
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
                _board.swap((int) _selectedSquareFirst.x,
                        (int) _selectedSquareFirst.y,
                        (int) _selectedSquareSecond.x,
                        (int) _selectedSquareSecond.y);

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
                        _board.del((int) _groupedSquares.get(i).get(j).x,
                                (int) _groupedSquares.get(i).get(j).y);
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

                // Steal some time
                _remainingTime -= _penaltyTime * 4;
                if (_points > 200) {
                    _points -= 200;
                } else {
                    _points = 0;
                }
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

        // Background image
        batch.draw(_imgBackground, 0, 0);

        // Draw buttons
        _hintButton.render();
        _resetButton.render();
        _musicButton.render();
        _exitButton.render();
        // _pauseButton.render();

        // Draw the score
        batch.draw(_imgScoreBackground, 295, 510);
        // Draw the time
        batch.draw(_imgTimeBackground, 56, 510);

        _layout.setText(_fontLCD, Integer.toString(_points));
        _fontLCD.draw(batch, Integer.toString(_points), 648 - _layout.width, 530);

        _layout.setText(_fontLCD, _txtTime);
        _fontLCD.draw(batch, _txtTime, 264 - _layout.width, 530);

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

                // If the mouse is over a gem
                if (overGem((int) _mousePos.x, (int) _mousePos.y)) {
                    // Draw the selector over that gem
                    Coord coord = getCoord((int) _mousePos.x, (int) _mousePos.y);
                    batch.draw(_imgSelector,
                            (int) gemsInitial.x + coord.x * 76,
                            (int) gemsInitial.y + coord.y * 76);
                }


                // If a gem was previously clicked
                if (_state == State.SelectedGem) {
                    // Draw the tinted selector over it
                    batch.setColor(1.0f, 0.0f, 1.0f, 1.0f);
                    batch.draw(_imgSelector,
                            (int) gemsInitial.x + _selectedSquareFirst.x * 76,
                            (int) gemsInitial.y + _selectedSquareFirst.y * 76);
                    batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                }
            }

            // If a hint is being shown
            if (_showingHint > 0.0) {
                // Get the opacity percentage
                float p = (float) (_showingHint / _animHintTotalTime);

                float x = gemsInitial.x + _coordHint.x * 76;
                float y = gemsInitial.y + _coordHint.y * 76;

                batch.draw(_imgHint, x, y);

            }
        }

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
    public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
        if (arg3 == 0) { // Left mouse button clicked
            _mousePos.x = arg0;
            _mousePos.y = arg1;
            _parent.getCamera().unproject(_mousePos);

            // Button
            if (_exitButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                _parent.changeState("StateMenu");
            } else if (_hintButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                showHint();
                // Penalty
                _remainingTime -= _penaltyTime * 2;
                if (_points > 100) {
                    _points -= 100;
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
            } else if (_resetButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                _state = State.DisappearingBoard;
                gemsOutScreen();
                resetGame();
            } else if (_pauseButton.isClicked((int) _mousePos.x, (int) _mousePos.y)) {
                if (_state == State.Wait) {
                    // PAUSE
                } else {
                    // RESUME
                }
            } else if (overGem((int) _mousePos.x, (int) _mousePos.y)) {
                _selectSFX.play();

                if (_state == State.Wait) { // No gems marked
                    _state = State.SelectedGem;
                    Coord coord = getCoord((int) _mousePos.x, (int) _mousePos.y);
                    _selectedSquareFirst.x = coord.x;
                    _selectedSquareFirst.y = coord.y;
                } else if (_state == State.SelectedGem) { // It is a marked gems
                    if (!checkClickedSquare((int) _mousePos.x, (int) _mousePos.y)) {
                        _selectedSquareFirst.x = -1;
                        _selectedSquareFirst.y = -1;
                        _state = State.Wait;
                    }
                }
            }
        }

        System.out.println("###" + _state.name() + "###");
        return false;
    }

    @Override
    public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
        if (arg3 == 0) { // Left mouse button clicked
            _mousePos.x = arg0;
            _mousePos.y = arg1;
            _parent.getCamera().unproject(_mousePos);

            if (_state == State.SelectedGem) {

                Coord res = getCoord((int) _mousePos.x, (int) _mousePos.y);

                if (!(res == _selectedSquareFirst)) {
                    checkClickedSquare((int) _mousePos.x, (int) _mousePos.y);
                }
            }

            _hintButton.touchUp();
            _musicButton.touchUp();
            _exitButton.touchUp();
            _resetButton.touchUp();
            _pauseButton.touchUp();
        }

        return false;
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
                    _fontLCD,
                    matchSize * 5 * _multiplier,
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
            _remainingTime += matchSize * _multiplier;
            _remainingTime -= _penaltyTime;
            _points += matchSize * 5 * _multiplier;
            if (_points >= 1500){
                _penaltyTime = 2;
            } else if (_points >= 3000) {
                _penaltyTime = 3;
            } else if (_points >= 5000) {
                _penaltyTime = 4;
            }
        }
    }

    /*  TODO: Fix gameplay bug!
        If the player fast enough then the game removes the wrong gems. Why?
    */
    private boolean checkClickedSquare(int mX, int mY) {
        _selectedSquareSecond = getCoord(mX, mY);

        // If gem is neighbour
        if (Math.abs(_selectedSquareFirst.x - _selectedSquareSecond.x)
                + Math.abs(_selectedSquareFirst.y - _selectedSquareSecond.y) == 1) {

            _board.swap(_selectedSquareFirst.x, _selectedSquareFirst.y,
                    _selectedSquareSecond.x, _selectedSquareSecond.y);

            _groupedSquares = _board.check();

            // If winning movement
            if (_groupedSquares.size != 0) {
                _state = State.ChangingGems;

                _board.swap(_selectedSquareFirst.x, _selectedSquareFirst.y,
                        _selectedSquareSecond.x, _selectedSquareSecond.y);

                return true;
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