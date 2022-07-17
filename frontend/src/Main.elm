module Main exposing (..)

import Browser
import Html exposing (..)
import Html.Events exposing (onClick)
import Http
import Json.Decode exposing (Decoder, map4, field, list, string)

-- MAIN

main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }

-- MODEL

type Model
  = Loading TweetPage
  | Success TweetPage
  | Failure TweetPage PostedBefore

type alias TweetPage =
  List Tweet

type alias Tweet =
  { id: String
  , author: String
  , contents: String
  , postedOn: String
  }

type alias PostedBefore =
  Maybe String

init : () -> (Model, Cmd Msg)
init _ =
  (Loading [], getPage Nothing)

-- UPDATE

type Msg
  = GetPage PostedBefore
  | GotPage PostedBefore (Result Http.Error TweetPage)

update : Msg -> Model -> (Model, Cmd Msg)
-- Try matchin on model first
update msg model =
  case msg of
    GetPage postedBefore ->
      case model of
        Loading previousTweetPages ->
          (Loading previousTweetPages, getPage postedBefore)

        Success previousTweetPages ->
          (Loading previousTweetPages, getPage postedBefore)

        Failure previousTweetPages _ ->
          (Loading previousTweetPages, getPage postedBefore)

    GotPage postedBefore result ->
      case result of
        Ok tweetPage ->
          case model of
            Loading previousTweetPages ->
              (Success (List.concat [ previousTweetPages, tweetPage ]), Cmd.none)

            Success previousTweetPages ->
              (Success (List.concat [ previousTweetPages, tweetPage ]), Cmd.none)

            Failure previousTweetPages _ ->
              (Success (List.concat [ previousTweetPages, tweetPage ]), Cmd.none)

        Err _ ->
          case model of
              Loading previousTweetPages ->
                (Failure previousTweetPages postedBefore, Cmd.none)

              Success previousTweetPages ->
                (Failure previousTweetPages postedBefore, Cmd.none)

              Failure previousTweetPages _ ->
                (Failure previousTweetPages postedBefore, Cmd.none)

-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions _ =
  Sub.none

-- VIEW

view : Model -> Html Msg
view model =
  case model of
    Loading _ ->
      text "Loading ..."

    Success tweetPage ->
      viewTweetPage tweetPage

    Failure _ postedBefore ->
      div []
        [ p [] [ text "Failed to load page" ]
        , button [ onClick (GetPage postedBefore) ] [ text "Try Again" ]
        ]

viewTweetPage : TweetPage -> Html Msg
viewTweetPage tweetPage =
  let
    nextPagePostedBefore = computeNextPagePostedBefore tweetPage
  in
    div []
      [ div[] (List.map viewTweet tweetPage)
      , button [ onClick (GetPage nextPagePostedBefore) ] [ text "Load more Tweets" ]
      ]

viewTweet : Tweet -> Html Msg
viewTweet tweet =
  div []
    [ h3 [] [ text tweet.author ]
    , h2 [] [ text tweet.contents ]
    , p [] [ text tweet.postedOn ]
    ]

computeNextPagePostedBefore : TweetPage -> PostedBefore
computeNextPagePostedBefore tweets =
  let
    selectEarliestDate : String -> Maybe String -> Maybe String
    selectEarliestDate date maybeOtherDate =
      case maybeOtherDate of
        Just otherDate ->
          if date < otherDate then
            Just date
          else
            Just otherDate
        Nothing ->
          Just date
  in
    List.foldl (selectEarliestDate) Nothing (List.map .postedOn tweets)

-- HTTP

getPage : PostedBefore -> Cmd Msg
getPage postedBefore =
  let
    query_params =
      case postedBefore of
        Just value ->
          String.concat [ "?page_size=1&posted_before=", value ]
        Nothing ->
          "?page_size=1"
  in
    Http.get
      { url = String.concat [ "http://localhost:8080/v1/tweets", query_params ]
      , expect = Http.expectJson (GotPage postedBefore) tweetPageDecoder
      }

tweetPageDecoder : Decoder TweetPage
tweetPageDecoder =
  list tweetDecoder

tweetDecoder : Decoder Tweet
tweetDecoder =
  map4 Tweet
    (field "id" string)
    (field "author" string)
    (field "contents" string)
    (field "postedOn" string)
