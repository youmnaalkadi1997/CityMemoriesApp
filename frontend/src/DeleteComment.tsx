import {useLocation, useNavigate, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";

export default function DeleteComment(){

    const { id } = useParams();
    const navigate = useNavigate();
    const [message, setMessage] = useState("");
    const location = useLocation();
    const params = new URLSearchParams(location.search);
    const cityName = params.get("city");

    useEffect(() => {
        axios.delete(`/api/comment/${id}`)
            .then(() => {
                setMessage("Kommentare wurde gelöscht");
                setTimeout(() => {
                    navigate(`/search?selected=${encodeURIComponent(cityName || "")}`);
                }, 1500);
            })
            .catch(() => {
                setMessage("Fehler beim Löschen");
            });
    }, [id, navigate, cityName]);

    return (
        <div className="container">
            <h2>Deleting Kommentare...</h2>
            <p>{message}</p>
        </div>
    );
}