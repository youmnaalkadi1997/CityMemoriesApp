import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate, useParams, useLocation } from "react-router-dom";

export default function EditComment() {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const [comment, setComment] = useState("");
    const [message, setMessage] = useState("");

    useEffect(() => {
        axios.get(`/api/comment/getId/${id}`)
            .then(res => {
                setComment(res.data.comment);
            })
            .catch(err => console.error(err));
    }, [id]);

    function updateComment(e: React.FormEvent) {
        e.preventDefault();

        const params = new URLSearchParams(location.search);
        const cityName = params.get("city");

        axios.put(`/api/comment/${id}`, { comment })
            .then(() => {
                setMessage("Kommentare erfolgreich aktualisiert");
                setTimeout(() => {
                    navigate(`/search?selected=${encodeURIComponent(cityName || "")}`);
                }, 1500);
            })
            .catch(() => {
                setMessage("Fehler beim Aktualisieren");
            });
    }

    return (
        <div className="container">
            <h2>Kommentare bearbeiten</h2>
            <form onSubmit={updateComment}>
                <label>
                    Kommentare:
                    <input
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                    />
                </label>
                <button>Änderungen speichern</button>
            </form>
            {message && <p>{message}</p>}
        </div>
    );
}