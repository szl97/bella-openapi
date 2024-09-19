import {auth} from "@/auth"
import {ClientHeader} from "@/components/client-header";

export default async function Home() {
  const session = await auth();
  return (<div>
    <ClientHeader/>
    <pre>{JSON.stringify(session, null, 2)}</pre>
  </div>)
}
