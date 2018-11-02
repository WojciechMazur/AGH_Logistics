import * as React from 'react';
import Button from '@material-ui/core/Button';
import {Field, Form, Formik} from 'formik';
import {LinearProgress} from '@material-ui/core';
import {TextField} from 'formik-material-ui';
import Dashboard from "../../../pages/transportDistribution/Dashboard";
import Grid from "@material-ui/core/Grid/Grid";
import {Recipient} from "../../../types";
import withStyles from "@material-ui/core/styles/withStyles";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

export interface Values {
    name: string;
    available?: number,
    demand?: number,
    limit?: number
}

type Props ={
    dashboardRef: Dashboard
}

function RecipientAddForm(props: Props) {
    const {classes} = props;

    return <Formik
        initialValues={{name: '', demand: '', available: '', limit: '', priority: ''}}
        validate={values => {
            const errors: Partial<Values> = {};
            if (!values.name) {
                errors.name = 'Required';
            }
            if (!values.demand) {
                errors.supply = 'Required'
            }else{
                if(values.demand <= 0){
                    errors.demand= 'Demand must be positive value'
                }
            }
            return errors;
        }}
        onSubmit={(values, {setSubmitting}) => {
            setSubmitting(false);
            props.dashboardRef.addRecipient(new Recipient(
                values.name,
                values.available || 0,
                values.priority,
                values.limit,
                values.demand
            ))
        }}
        render={({submitForm, isSubmitting}) => (
            <Form>
                <Grid
                    className={classes.root}
                    container
                    justify="center"
                    alignItems="center"
                    spacing={16}
                >
                    <Grid item xs>
                        <Field
                            name="name"
                            type="text"
                            label="Recipient name"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="demand"
                            type="number"
                            label="Maximal demand"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="available"
                            type="number"
                            label="Currently available"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="limit"
                            type="number"
                            label="Disposal limit"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="priority"
                            type="number"
                            label="Recipient priority"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        {isSubmitting && <LinearProgress/>}
                        <br/>
                        <Button
                            variant="contained"
                            color="primary"
                            disabled={isSubmitting}
                            onClick={submitForm}
                        >
                            <FontAwesomeIcon icon={"plus"}/>
                        </Button>
                    </Grid>
                </Grid>
            </Form>
        )}
    />
}

const styles = theme => ({
    root:{
        padding: theme.spacing.unit * 2
    }
});

export default withStyles(styles)(RecipientAddForm)

