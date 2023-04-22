module Pages.Tweets.Id_ exposing (Model, Msg, page)

import Frame
import Gen.Params.Tweets.Id_ exposing (Params)
import Html exposing (Html, p, text)
import Http
import Model.Tweets exposing (Tweet, tweetDecoder, viewTweet)
import Page
import Request
import Shared
import View exposing (View)


page : Shared.Model -> Request.With Params -> Page.With Model Msg
page _ req =
    Page.element
        { init = init req
        , update = update
        , view = view req
        , subscriptions = subscriptions
        }



-- INIT


type Model
    = Loading
    | Failure
    | Success Tweet


init : Request.With Params -> ( Model, Cmd Msg )
init req =
    ( Loading
    , Http.get
        { url = "http://localhost:8080/api/v2/tweets/" ++ req.params.id
        , expect = Http.expectJson GotResponse tweetDecoder
        }
    )



-- UPDATE


type Msg
    = GotResponse (Result Http.Error Tweet)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GotResponse res ->
            case res of
                Ok tweet ->
                    ( Success tweet, Cmd.none )

                Err _ ->
                    ( Failure, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none



-- VIEW


view : Request.With Params -> Model -> View Msg
view req model =
    { title = "Tweet #" ++ req.params.id
    , body = viewBody model
    }


viewBody : Model -> List (Html Msg)
viewBody model =
    case model of
        Loading ->
            Frame.apply [ text "Loading Tweet ... " ]

        Failure ->
            Frame.apply [ text "Failed to load Tweet" ]

        Success tweet ->
            Frame.apply
                [ viewTweet tweet ]
