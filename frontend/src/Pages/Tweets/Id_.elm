module Pages.Tweets.Id_ exposing (Model, Msg, page)

import Dict
import Gen.Params.Tweets.Id_ exposing (Params)
import Html exposing (div, text)
import Page
import Request
import Shared
import View exposing (View)


page : Shared.Model -> Request.With Params -> Page.With Model Msg
page shared req =
    Page.element
        { init = init
        , update = update
        , view = view req
        , subscriptions = subscriptions
        }



-- INIT


type alias Model =
    {}


init : ( Model, Cmd Msg )
init =
    ( {}, Cmd.none )



-- UPDATE


type Msg
    = ReplaceMe


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        ReplaceMe ->
            ( model, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- VIEW


view : Request.With Params -> Model -> View Msg
view req model =
    { title = "Tweet"
    , body = [ req.params.id |> (\id -> text ("This is the Tweet with ID " ++ id)) ]
    }
