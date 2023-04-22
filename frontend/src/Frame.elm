module Frame exposing (apply)

import Html exposing (Html, br, div, p, section, text)
import Html.Attributes exposing (class, style)


apply : List (Html msg) -> List (Html msg)
apply contents =
    [ section [ class "hero is-small is-info" ]
        [ div [ class "hero-body" ]
            [ p [ class "title" ] [ text "TwitterClone" ]
            , p [ class "subtitle" ] [ text "The bird is freed" ]
            ]
        ]
    , br [] []
    , div [ class "container", style "max-width" "600px" ] contents
    ]
