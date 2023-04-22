module Frame exposing (apply)

import Html exposing (Html, div, p, section, text)
import Html.Attributes exposing (class)


apply : List (Html msg) -> List (Html msg)
apply contents =
    [ section [ class "hero is-small is-info" ]
        [ div [ class "hero-body" ]
            [ p [ class "title" ] [ text "TwitterClone" ]
            , p [ class "subtitle" ] [ text "The bird is freed" ]
            ]
        ]
    , div [ class "container" ] contents
    ]
