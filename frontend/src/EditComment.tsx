import {type ChangeEvent, type FormEvent, useEffect, useState} from "react";
import axios from "axios";
import { useNavigate, useParams, useLocation } from "react-router-dom";

export default function EditComment() {
    const { id } = useParams();
    const navigate = useNavigate();
    const location = useLocation();

    const [comment, setComment] = useState("");
    const [message, setMessage] = useState("");
    const [image, setImage] = useState<File>();
    const [preview, setPreview] = useState<string>();

    useEffect(() => {
        axios.get(`/api/comment/getId/${id}`)
            .then(res => {
                setComment(res.data.comment);
                setPreview(res.data.imageUrl);
            })
            .catch(err => console.error(err));
    }, [id]);

    function onFileChange(e: ChangeEvent<HTMLInputElement>) {
        if (e.target.files) {
            const file = e.target.files[0];
            setImage(file);
            setPreview(URL.createObjectURL(file));
        }
    }

    function updateComment(e: FormEvent) {
        e.preventDefault();

        const params = new URLSearchParams(location.search);
        const cityName = params.get("city");

        const formData = new FormData();
        const json = { comment };
        formData.append("data", new Blob([JSON.stringify(json)], { type: "application/json" }));

        if (image) {
            formData.append("file", image);
        }

        axios.put(`/api/comment/${id}`, formData, {
            headers: { "Content-Type": "multipart/form-data" },
        })
            .then(() => {
                setMessage("Kommentar erfolgreich aktualisiert!");
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
                <label>
                    Neues Bild (optional):
                    <input type="file" accept="image/*" onChange={onFileChange} />
                </label>

                {preview && (
                    <div>
                        <p>Vorschau:</p>
                        <img src={preview} alt="preview" width="200" />
                    </div>
                )}
                <button>Änderungen speichern</button>
            </form>
            {message && <p>{message}</p>}
        </div>
    );
}