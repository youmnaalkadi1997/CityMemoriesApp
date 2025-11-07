import {type ChangeEvent, useEffect, useState} from "react";
import axios from "axios";
import {Link, useLocation, useNavigate} from "react-router-dom";
import { FaHeart, FaRegHeart } from "react-icons/fa";

type WikiData = {
    title: string;
    extract: string;
    thumbnail?: {
        source: string;
        width: number;
        height: number;
    };
    content_urls?: {
        desktop: {
            page: string;
        };
    };
};

type CityComment = {
    id: string;
    cityName: string;
    username: string;
    comment: string;
    imageUrl?: string;
    createdAt: string;
    updatedAt: string;
    likesCount: number;
    likedByUsernames: string[];
    replies?: {
        id: string;
        username: string;
        text: string;
        createdAt: string;
    }[];
};

type Props = {
    readonly  cityName: string;
    readonly  user: string |undefined|null
};

function handleReplyError(error: unknown) {
    console.error(error);
}

export default function CitySummary({ cityName, user }: Props) {
    const [data, setData] = useState<WikiData | null>(null);
    const [loading, setLoading] = useState(false);
    const [comment, setComment] = useState("");
    const [message, setMessage] = useState("");
    const [comments, setComments] = useState<CityComment[]>([]);
    const [image, setImage] = useState<File>();
    const [favMessage, setFavMessage] = useState("");
    const [isFavorite, setIsFavorite] = useState(false);
    const navigate = useNavigate();
    const [replyTexts, setReplyTexts] = useState<{ [key: string]: string }>({});
    const location = useLocation();
    const [fileName, setFileName] = useState<string>("");




    useEffect(() => {
        if (!cityName) return;

        setLoading(true);

        axios.get(`https://en.wikipedia.org/api/rest_v1/page/summary/${encodeURIComponent(cityName)}`)
            .then((res) => setData(res.data))
            .catch((err) => {
                console.error("Wiki Fehler", err);
                setData(null);
            });

        axios.get(`/api/comment/${encodeURIComponent(cityName)}`)
            .then((res) => setComments(res.data))
            .catch((err) => {
                console.error("Comment Fehler", err);
                setComments([]);
            })
            .finally(() => setLoading(false));

    }, [cityName]);

    useEffect(() => {
        const timer = setTimeout(() => {
            setMessage("");
            setFavMessage("");
        }, 3000);

        return () => clearTimeout(timer);
    }, [message, favMessage]);

    useEffect(() => {
        if (!user) return;

        axios.get("/api/favorites",{params : {username : user}})
            .then(res => {
                const favoriteCities: string[] = res.data;
                setIsFavorite(favoriteCities.includes(cityName));
            })
            .catch(err => console.error(err));
    }, [cityName, user]);

    useEffect(() => {
        if (!location.hash) return;
        const el = document.querySelector(location.hash);
        if (el) {
            el.scrollIntoView({ behavior: "smooth", block: "center" });
            el.classList.add("highlight");
            setTimeout(() => el.classList.remove("highlight"), 3000);
        }
    }, [location.hash, comments]);

    function addComment(event?: React.FormEvent<HTMLFormElement>) {
        event?.preventDefault();
        const data = new FormData();

        if (image) {
            data.append("file", image);
        }

        const json = {
            cityName: cityName,
            username: user,
            comment: comment
        };

        data.append("data", new Blob([JSON.stringify(json)], { type: "application/json" }));

        axios.post("/api/addcomment", data, {
            headers: { "Content-Type": "multipart/form-data" }
        })
            .then(() => {
                setMessage("Kommentar erfolgreich hinzugefügt");
                setComment("");
                setImage(undefined);
                return axios.get(`/api/comment/${encodeURIComponent(cityName)}`);
            })
            .then(res => setComments(res.data))
            .catch((error) => {
                if (error.response) {
                    setMessage(error.response.data?.message || "Fehler beim Hochladen");
                } else {
                    setMessage("Netzwerkfehler oder Server nicht erreichbar");
                }
            });
    }

    function onFileChange(event: ChangeEvent<HTMLInputElement>) {
        if (event.target.files) {
            setImage(event.target.files[0])
            setFileName (event.target.files[0].name)
        }
    }

    function toggleFavorite() {
        if (!user) {
            setFavMessage("Bitte einloggen, um die Stadt zu favorisieren.");
            return;
        }

        if (isFavorite) {
            axios.delete(`/api/deleteFromFav/${encodeURIComponent(cityName)}` ,{ params : {username : user}})
                .then(() => {
                    setIsFavorite(false);
                    setFavMessage("Stadt aus Favoriten entfernt!");
                })
                .catch(err => {
                    console.error(err);
                    setFavMessage("Fehler beim Entfernen aus Favoriten.");
                });
        } else {
            axios.post("/api/addToFavorites", {
                cityName: cityName,
                username: user
            })
                .then(() => {
                    setIsFavorite(true);
                    setFavMessage("Stadt zur Favoritenliste hinzugefügt!");
                })
                .catch(err => {
                    console.error(err);
                    setFavMessage("Fehler beim Hinzufügen zur Favoritenliste.");
                });
        }
    }

    function updateCommentLikes(
        comments: CityComment[],
        commentId: string,
        data: { likesCount: number; likedByUsernames: string[] }
    ): CityComment[] {
        return comments.map(com =>
            com.id === commentId
                ? { ...com, likesCount: data.likesCount, likedByUsernames: data.likedByUsernames }
                : com
        );
    }

    function updateCommentWithReply(
        comments: CityComment[],
        commentId: string,
        newComment: CityComment
    ): CityComment[] {
        return comments.map(c => (c.id === commentId ? newComment : c));
    }

    function toggleLikeComment(commentId: string) {
        if (!user) {
            alert("Bitte einloggen, um zu liken.");
            return;
        }

        axios
            .post(`/api/comment/${commentId}/like`, null, { params: { username: user } })
            .then(res => {
                setComments(prev => updateCommentLikes(prev, commentId, res.data));
            })
            .catch(console.error);
    }

    function handleReplySuccess(commentId: string, newComment: CityComment) {
        setComments(prev => updateCommentWithReply(prev, commentId, newComment));
        setReplyTexts(prev => ({ ...prev, [commentId]: "" }));
    }



    function addReply(commentId: string) {
        if (!user) {
            alert("Bitte einloggen, um zu antworten.");
            return;
        }

        const text = replyTexts[commentId];
        if (!text || text.trim() === "") return;

        axios
            .post(`/api/comment/${commentId}/reply`, {
                username: user,
                text: text,
            })
            .then(res => handleReplySuccess(commentId, res.data))
            .catch(handleReplyError);
    }


    if (loading) return <p>Lade Beschreibung...</p>;
    if (!data) return <p>Keine Beschreibung gefunden.</p>;

    return (
        <div className="city-summary">
            <h3>{data.title}</h3>
            <button onClick={toggleFavorite}>
                {isFavorite ? (
                    <FaHeart style={{ color: "red", fontSize: "1.5rem" }} />
                ) : (
                    <FaRegHeart style={{ color: "gray", fontSize: "1.5rem" }} />
                )}
            </button>
            {favMessage && <p>{favMessage}</p>}

            {data.thumbnail && (
                <img src={data.thumbnail.source} alt={data.title} width="100%" />
            )}

            <p>{data.extract}</p>

            {data.content_urls?.desktop.page && (
                <p>
                    Quelle:{" "}
                    <a href={data.content_urls.desktop.page} target="_blank" rel="noopener noreferrer">
                        Wikipedia – {data.title}
                    </a>
                </p>
            )}

            <form className="form" onSubmit={addComment}>
                <div className="textarea-wrapper">
                <textarea
                    value={comment}
                    className="input"
                    onChange={(e) => setComment(e.target.value)}
                    rows={4}
                    placeholder="Kommentieren .."
                />
                <input
                    type="file"
                    accept="image/*"
                    id="fileInput"
                    style={{ display: "none" }}
                    onChange={onFileChange}
                />
                <label htmlFor="fileInput" className="camera-button">📷</label>
                </div>
                {fileName && (
                    <p className="file-name">📎 {fileName}</p>
                )}
                <button className="button" type="submit">Senden</button>
                {message && (
                    <p>
                        {message}
                    </p>
                )}
            </form>
            <div>
                <h4>Kommentare:</h4>
                {comments.length === 0 ? (
                    <p>Keine Kommentare für diese Stadt.</p>
                ) : (
                    <ul>
                        {comments.map(c => {

                            const likedByUser = (c.likedByUsernames || []).includes(user || "");

                            return (
                                <li key={c.id} id={`comment-${c.id}`}  className="comment-box">
                                    <strong>{c.username}</strong> schrieb am {new Date(c.createdAt).toLocaleString()}:
                                    <p>{c.comment}</p>

                                    {c.updatedAt && c.updatedAt !== c.createdAt && (
                                        <p style={{ color: "grey" }}>
                                            Zuletzt bearbeitet am: {new Date(c.updatedAt).toLocaleString()}
                                        </p>
                                    )}

                                    {c.imageUrl && (
                                        <button
                                            type="button"
                                            onClick={() => window.open(c.imageUrl, "_blank")}
                                            style={{
                                                all: "unset",
                                                cursor: "pointer",
                                                display: "inline-block",
                                                padding: 0,
                                                border: "none",
                                            }}
                                        >
                                            <img
                                                src={c.imageUrl}
                                                alt="Bild zur Stadt"
                                                style={{ maxWidth: "200px", display: "block" }}
                                            />
                                        </button>
                                    )}

                                    <div className="link-buttons">
                                        {user === c.username && (
                                            <>
                                                <Link to={`/edit/${c.id}?city=${encodeURIComponent(cityName)}`}>
                                                    <button>✏️</button>
                                                </Link>
                                                <button onClick={() => {
                                                    const confirmDelete = window.confirm("Willst du diesen Kommentar wirklich löschen?");
                                                    if(confirmDelete) navigate(`/delete/${c.id}?city=${encodeURIComponent(cityName)}`);
                                                }}>🗑️</button>
                                            </>
                                        )}

                                        <div
                                            className="like-container"
                                            style={{ position: "relative", display: "inline-block" }}
                                        >
                                            <button
                                                onClick={() => toggleLikeComment(c.id)}
                                                style={{
                                                    color: likedByUser ? "red" : "grey",
                                                    fontWeight: "bold",
                                                    marginLeft: "10px"
                                                }}
                                            >
                                                ❤️ {c.likesCount ?? 0}
                                            </button>
                                            {c.likedByUsernames && c.likedByUsernames.length > 0 && (
                                                <div className="tooltip">
                                                    {user && c.likedByUsernames.includes(user)
                                                        ? c.likedByUsernames.length === 1
                                                            ? "du"
                                                            : `du und ${c.likedByUsernames.length - 1} andere`
                                                        : c.likedByUsernames.slice(0, 3).join(", ") +
                                                        (c.likedByUsernames.length > 3
                                                            ? ` und ${c.likedByUsernames.length - 3} andere`
                                                            : "")
                                                    }
                                                </div>
                                            )}
                                        </div>


                                    </div>

                                    <div style={{ marginTop: "8px" }}>
                                        <button
                                            onClick={() =>
                                                setReplyTexts(prev => {
                                                    const newState = { ...prev };

                                                    if (newState[c.id] !== undefined) {
                                                        delete newState[c.id];
                                                    } else {
                                                        newState[c.id] = "";
                                                    }

                                                    return newState;
                                                })
                                            }
                                        >
                                            💬
                                        </button>

                                        {replyTexts[c.id] !== undefined && (
                                            <div style={{ marginTop: "6px" }}>
                                                <input
                                                    type="text"
                                                    placeholder="Schreibe hier deine Antwort…"
                                                    value={replyTexts[c.id]}
                                                    onChange={(e) =>
                                                        setReplyTexts(prev => ({ ...prev, [c.id]: e.target.value }))
                                                    }
                                                    style={{
                                                        padding: "6px",
                                                        borderRadius: "6px",
                                                        border: "1px solid #ccc",
                                                        width: "70%",
                                                        marginRight: "5px"
                                                    }}
                                                />
                                                <button onClick={() => addReply(c.id)}>Senden</button>
                                            </div>
                                        )}
                                    </div>

                                    {c.replies && c.replies.length > 0 && (
                                        <ul style={{ marginLeft: "20px", marginTop: "10px" }}>
                                            {c.replies.map(r => (
                                                <li key={r.id} style={{ borderLeft: "2px solid #ddd", paddingLeft: "8px" }}>
                                                    <strong>{r.username}</strong>: {r.text}
                                                    <p style={{ fontSize: "12px", color: "gray" }}>
                                                        {new Date(r.createdAt).toLocaleString()}
                                                    </p>
                                                    {user === r.username && (
                                                        <button
                                                            style={{ marginLeft: "10px" }}
                                                            onClick={() => {
                                                                const confirmDelete = window.confirm("Willst du diese Antwort wirklich löschen?");
                                                                if (!confirmDelete) return;

                                                                axios.delete(`/api/comment/${c.id}/reply/${r.id}`, { params: { username: user } })
                                                                    .then(res => {
                                                                        setComments(prev => prev.map(com => com.id === c.id ? res.data : com));
                                                                    })
                                                                    .catch(err => console.error(err));
                                                            }}
                                                        >
                                                            🗑️ Löschen
                                                        </button>
                                                    )}
                                                </li>
                                            ))}
                                        </ul>
                                    )}

                                </li>
                            );
                        })}

                    </ul>
                )}
            </div>


        </div>
    );
}