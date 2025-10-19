import './App.css'
import CitySearch from "./CitySearch.tsx";
import {useEffect, useState} from "react";
import axios from "axios";
import {Route, Routes} from "react-router-dom";
import Login from "./Login.tsx";
import ProtectedRout from "./ProtectedRout.tsx";
import EditComment from "./EditComment.tsx";
import DeleteComment from "./DeleteComment.tsx";

function App() {

    const [user, setUser] = useState<string | undefined | null>(undefined);

    useEffect(() => {
        axios.get('/api/auth/me', { withCredentials: true })
            .then(response => {
                setUser(response.data);
            })
            .catch(error => {
                setUser(null);
                console.log("Not authenticated", error);
            });
    }, []);

  return (
        <Routes>
            <Route path={"/"} element={<Login />}></Route>
            <Route element={<ProtectedRout user={user}/>}>
                <Route path={"/search"} element={<CitySearch user={user}/>}></Route>
                <Route path="/edit/:id" element={<EditComment />} />
                <Route path="/delete/:id" element={<DeleteComment />} />
            </Route>
        </Routes>

  )
}

export default App
