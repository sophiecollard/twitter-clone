module ViewUtils.Hero exposing (view)

import Html exposing (Html, div, p, section, text)
import Html.Attributes exposing (class)


view : Html msg
view =
    section [ class "hero is-small is-info" ]
        [ div [ class "hero-body" ]
            [ p [ class "title" ] [ text "TwitterClone" ]
            , p [ class "subtitle" ] [ text "The bird is freed" ]
            ]
        ]
