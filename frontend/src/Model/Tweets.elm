module Model.Tweets exposing (Tweet, tweetDecoder, viewTweet)

import Html exposing (Html, button, div, figure, img, p, span, strong, text)
import Html.Attributes exposing (class, src)
import Json.Decode exposing (Decoder)



-- MODEL


type alias Tweet =
    { id : String
    , authorId : String
    , contents : String
    , postedOn : String
    }


tweetDecoder : Decoder Tweet
tweetDecoder =
    Json.Decode.map4 Tweet
        (Json.Decode.field "id" Json.Decode.string)
        (Json.Decode.field "authorId" Json.Decode.string)
        (Json.Decode.field "contents" Json.Decode.string)
        (Json.Decode.field "postedOn" Json.Decode.string)



-- VIEW


viewTweet : Tweet -> Html msg
viewTweet tweet =
    div [ class "card" ]
        [ div [ class "card-content" ]
            [ div [ class "media" ]
                [ div [ class "media-left" ]
                    [ figure [ class "image is-48x48" ]
                        [ img [ src "https://bulma.io/images/placeholders/96x96.png" ] []
                        ]
                    ]
                , div [ class "media-content" ]
                    [ p [ class "title is-5" ] [ text tweet.authorId ] -- FIXME Replace with username
                    , p [ class "subtitle is-6" ] [ text ("@" ++ tweet.authorId) ] -- FIXME Replace with handle
                    ]
                ]
            , div [ class "content" ]
                [ p [ class "title" ] [ text tweet.contents ]
                , p [ class "subtitle is-6" ] [ text tweet.postedOn ]
                ]
            ]
        , div [ class "card-footer" ]
            [ p [ class "card-footer-item" ] [ span [] [ strong [] [ text "0" ], text " Likes" ] ]
            , p [ class "card-footer-item" ] [ viewLikeButton False ]
            ]
        ]


viewLikeButton : Bool -> Html msg
viewLikeButton liked =
    if liked then
        button [ class "button is-info is-fullwidth" ] [ text "Unlike" ]

    else
        button [ class "button is-info is-outlined is-fullwidth" ] [ text "Like" ]
