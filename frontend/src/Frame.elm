module Frame exposing (apply)

import Html exposing (Html, a, br, div, h1, nav, section, span, text)
import Html.Attributes exposing (class, style)


apply : List (Html msg) -> List (Html msg)
apply contents =
    [ section [ class "hero is-small is-info" ]
        [ div [ class "hero-head" ]
            [ nav [ class "navbar" ]
                [ div [ class "container" ]
                    [ div [ class "navbar-brand" ]
                        [ div [ class "navbar-item" ]
                            [ h1 [ class "title" ] [ text "TwitterClone" ]
                            ]
                        , span [ class "navbar-burger" ]
                            [ span [] []
                            , span [] []
                            , span [] []
                            ]
                        ]
                    , div [ class "navbar-menu" ]
                        [ div [ class "navbar-end" ]
                            [ a [ class "navbar-item" ]
                                [ span [ class "has-text-weight-semibold" ] [ text "Log in" ] ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
    , br [] []
    , div [ class "container", style "max-width" "600px" ] contents
    ]
