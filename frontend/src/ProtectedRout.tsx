import {Navigate, Outlet} from "react-router-dom";

type ProtectedRoutProps = {
    user: string |undefined|null
}

export default function ProtectedRout(props:Readonly<ProtectedRoutProps>){

    if(props.user === undefined)
    {
        return  <h3>Loading</h3>
    }

    return (
        props.user ? <Outlet /> : <Navigate to = "/" />
    )

}