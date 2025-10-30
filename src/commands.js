//Auth Commands
//Change the words inside to work with our specific data, but this is a placeholder for database functionality


//signup
const { data: signUpData, error } = await supabase.auth.signUp({
    email: 'example@email.com',
    password: 'yourpassword',
  })
//login
await supabase.auth.signInWithPassword({
  email: 'example@email.com',
  password: 'yourpassword',
})
//import at the top of js file
import { createClient } from '@supabase/supabase-js'

//Insert Data
await supabase.from('students').insert([{ name: 'Placeholder', grade: 95 }])

//Update Data
await supabase.from('students').update({ grade: 97 }).eq('name', 'Placeholder')
//DELETE Data
await supabase.from('students').delete().eq('name', 'Placeholder')

//Fetch Data
const { data: studentsData } = await supabase.from('students').select('name, grade')

//Connect to Supabase
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = import.meta.env.SUPABASE_URL
const supabaseKey = import.meta.env.SUPABASE_KEY

export const supabase = createClient(supabaseUrl, supabaseKey)