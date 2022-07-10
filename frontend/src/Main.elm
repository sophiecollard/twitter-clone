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
  = Loading
  | Success TweetPage
  | Failure PostedBefore

type alias PostedBefore =
  Maybe String

type alias TweetPage =
  List Tweet

type alias Tweet =
  { id: String
  , author: String
  , contents: String
  , postedOn: String
  }

init : () -> (Model, Cmd Msg)
init _ =
  (Loading, getPage Nothing)

-- UPDATE

type Msg
  = GetPage PostedBefore
  | GotPage PostedBefore (Result Http.Error TweetPage)

update : Msg -> Model -> (Model, Cmd Msg)
update msg _ =
  case msg of
    GetPage postedBefore ->
      (Loading, getPage postedBefore)

    GotPage postedBefore result ->
      case result of
        Ok tweetPage ->
          (Success tweetPage, Cmd.none)

        Err _ ->
          (Failure postedBefore, Cmd.none)

-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none

-- VIEW

view : Model -> Html Msg
view model =
  case model of
    Loading ->
      text "Loading ..."

    Success tweetPage ->
      viewTweetPage tweetPage

    Failure postedBefore ->
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
      , button [ onClick (GetPage nextPagePostedBefore) ] [ text "Next Page" ]
      ]

viewTweet : Tweet -> Html Msg
viewTweet tweet =
  div []
    [ h2 [] [ text tweet.author ]
    , p [] [ text tweet.contents ]
    , p [] [ text tweet.postedOn ]
    ]

computeNextPagePostedBefore : TweetPage -> PostedBefore
computeNextPagePostedBefore tweets =
  let
    computeEarlierDate : String -> Maybe String -> Maybe String
    computeEarlierDate date maybeOtherDate =
      case maybeOtherDate of
        Just otherDate ->
          if date < otherDate then
            Just date
          else
            Just otherDate
        Nothing ->
          Just date
  in
    List.foldl (computeEarlierDate) Nothing (List.map .postedOn tweets)

-- HTTP

getPage : PostedBefore -> Cmd Msg
getPage postedBefore =
  let
    query_params =
      case postedBefore of
        Just value ->
          String.concat [ "?posted_before=", value ]
        Nothing ->
          ""
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
