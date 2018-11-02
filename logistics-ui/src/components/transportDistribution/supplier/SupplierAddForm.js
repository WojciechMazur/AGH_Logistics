import * as React from 'react';
import Button from '@material-ui/core/Button';
import {Field, Form, Formik} from 'formik';
import {LinearProgress} from '@material-ui/core';
import {TextField} from 'formik-material-ui';
import Dashboard from "../../../pages/transportDistribution/Dashboard";
import Grid from "@material-ui/core/Grid/Grid";
import {Supplier} from "../../../types";
import withStyles from "@material-ui/core/styles/withStyles";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

export interface Values {
    name: string;
    available?: number,
    supply?: number,
    limit?: number
}

type Props ={
    dashboardRef: Dashboard
}

function SupplierAddForm(props: Props) {
    const {classes} = props;

    return <Formik
        initialValues={{name: '', supply: '', available: '', limit: '', priority: ''}}
        validate={values => {
            const errors: Partial<Values> = {};
            if (!values.name) {
                errors.name = 'Required';
            }
            if (!values.supply) {
                errors.supply = 'Required'
            }else{
                if(values.supply <= 0){
                    errors.supply = 'Supply must be positive value'
                }
            }
            return errors;
        }}
        onSubmit={(values, {setSubmitting}) => {
            setSubmitting(false);
            props.dashboardRef.addSupplier(new Supplier(
                values.name,
                values.available || values.supply,
                values.priority,
                values.limit,
                values.supply
            ));
        }}
        render={({submitForm, isSubmitting}) => (
            <Form>
                <Grid
                    className={classes.root}
                    container
                    justify="center"
                    alignItems="center"
                    spacing={24}
                >
                    <Grid item xs>
                        <Field
                            name="name"
                            type="text"
                            label="Supplier name"
                            component={TextField}
                        />
                    </Grid>
                    <Grid item xs>
                        <Field
                            name="supply"
                            type="number"
                            label="Maximal supply"
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
                            label="Supplier priority"
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
      padding: theme.spacing.unit * 2,
  },

});

export default withStyles(styles)(SupplierAddForm)

